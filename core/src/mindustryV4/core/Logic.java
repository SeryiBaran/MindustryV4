package mindustryV4.core;

import com.badlogic.gdx.utils.Array;
import io.anuke.annotations.Annotations.Loc;
import io.anuke.annotations.Annotations.Remote;
import mindustryV4.Vars;
import mindustryV4.core.GameState.State;
import mindustryV4.entities.TileEntity;
import mindustryV4.game.EventType.*;
import mindustryV4.game.GameMode;
import mindustryV4.game.Team;
import mindustryV4.game.Teams;
import mindustryV4.game.UnlockableContent;
import mindustryV4.gen.Call;
import mindustryV4.net.Net;
import mindustryV4.type.ItemStack;
import mindustryV4.type.Recipe;
import mindustryV4.world.Tile;
import ucore.core.Events;
import ucore.core.Timers;
import ucore.entities.Entities;
import ucore.entities.EntityGroup;
import ucore.entities.EntityQuery;
import ucore.modules.Module;

import static mindustryV4.Vars.*;

/**
 * Logic module.
 * Handles all logic for entities and waves.
 * Handles game state events.
 * Does not store any game state itself.
 * <p>
 * This class should <i>not</i> call any outside methods to change state of modules, but instead fire events.
 */
public class Logic extends Module{

    public Logic(){
        Events.on(TileChangeEvent.class, event -> {
            if(event.tile.getTeam() == defaultTeam && Recipe.getByResult(event.tile.block()) != null){
                handleContent(Recipe.getByResult(event.tile.block()));
            }
        });
    }

    @Override
    public void init(){
        EntityQuery.init();
        EntityQuery.collisions().setCollider(tilesize, (x, y) -> {
            Tile tile = world.tile(x, y);
            return tile != null && tile.solid();
        });
    }

    /**Handles the event of content being used by either the player or some block.*/
    public void handleContent(UnlockableContent content){
        if(world.getSector() != null){
            world.getSector().currentMission().onContentUsed(content);
        }

        if(!headless){
            control.unlocks.unlockContent(content);
        }
    }

    public void play(){
        state.set(State.playing);
        state.wavetime = wavespace * state.difficulty.timeScaling * 2;

        for(Tile tile : state.teams.get(defaultTeam).cores){
            if(world.getSector() != null){
                Array<ItemStack> items = world.getSector().startingItems;
                for(ItemStack stack : items){
                    tile.entity.items.add(stack.item, stack.amount);
                }
            }
        }

        Events.fire(new PlayEvent());
    }

    public void reset(){
        state.wave = 1;
        state.wavetime = wavespace * state.difficulty.timeScaling;
        state.gameOver = false;
        state.teams = new Teams();

        Timers.clear();
        Entities.clear();
        TileEntity.sleepingEntities = 0;

        Events.fire(new ResetEvent());
    }

    public void runWave(){
        world.spawner.spawnEnemies();
        state.wave++;
        state.wavetime = wavespace * state.difficulty.timeScaling;

        Events.fire(new WaveEvent());
    }

    private void checkGameOver(){
        if(!state.mode.isPvp && state.teams.get(defaultTeam).cores.size == 0 && !state.gameOver){
            state.gameOver = true;
            Events.fire(new GameOverEvent(waveTeam));
        }else if(state.mode.isPvp){
            Team alive = null;

            for(Team team : Team.all){
                if(state.teams.get(team).cores.size > 0){
                    if(alive != null){
                        return;
                    }
                    alive = team;
                }
            }

            if(alive != null && !state.gameOver){
                state.gameOver = true;
                Events.fire(new GameOverEvent(alive));
            }
        }
    }

    private void updateSectors(){
        if(world.getSector() == null || state.gameOver) return;

        world.getSector().currentMission().update();

        //check unlocked sectors
        while(!world.getSector().complete && world.getSector().currentMission().isComplete()){
            Call.onMissionFinish(world.getSector().completedMissions);
        }

        //check if all assigned missions are complete
        if(!world.getSector().complete && world.getSector().completedMissions >= world.getSector().missions.size){
            Call.onSectorComplete();
        }
    }

    @Remote(called = Loc.both)
    public static void onGameOver(Team winner){
        threads.runGraphics(() -> ui.restart.show(winner));
        netClient.setQuiet();
    }

    @Remote(called = Loc.server)
    public static void onMissionFinish(int index){
        world.getSector().missions.get(index).onComplete();
        world.getSector().completedMissions = index + 1;

        state.mode = world.getSector().currentMission().getMode();
        world.getSector().currentMission().onBegin();
        world.sectors.save();
    }

    @Remote(called = Loc.server)
    public static void onSectorComplete(){
        state.mode = GameMode.victory;

        world.sectors.completeSector(world.getSector().x, world.getSector().y);
        world.sectors.save();

        if(!headless && !Net.client()){
            ui.missions.show(world.getSector());
        }

        Events.fire(new SectorCompleteEvent());
    }

    @Override
    public void update(){

        if(Vars.control != null){
            control.runUpdateLogic();
        }

        if(!state.is(State.menu)){

            if(!state.isPaused()){
                Timers.update();

                if(!state.mode.disableWaveTimer && !state.mode.disableWaves && !state.gameOver){
                    state.wavetime -= Timers.delta();
                }

                if(!Net.client() && state.wavetime <= 0 && !state.mode.disableWaves){
                    runWave();
                }

                if(!Entities.defaultGroup().isEmpty())
                    throw new RuntimeException("Do not add anything to the default group!");

                if(!headless){
                    Entities.update(effectGroup);
                    Entities.update(groundEffectGroup);
                }

                for(EntityGroup group : unitGroups){
                    Entities.update(group);
                }

                Entities.update(puddleGroup);
                Entities.update(shieldGroup);
                Entities.update(bulletGroup);
                Entities.update(tileGroup);
                Entities.update(fireGroup);
                Entities.update(playerGroup);

                //effect group only contains item transfers in the headless version, update it!
                if(headless){
                    Entities.update(effectGroup);
                }

                for(EntityGroup group : unitGroups){
                    if(group.isEmpty()) continue;

                    EntityQuery.collideGroups(bulletGroup, group);
                }

                EntityQuery.collideGroups(bulletGroup, playerGroup);
                EntityQuery.collideGroups(playerGroup, playerGroup);

                world.pathfinder.update();
            }

            if(!Net.client() && !world.isInvalidMap()){
                updateSectors();
                checkGameOver();
            }
        }
    }
}
