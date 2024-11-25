package mindustryV4.world.consumers;

import com.badlogic.gdx.utils.Array;
import mindustryV4.entities.TileEntity;
import mindustryV4.type.Liquid;
import mindustryV4.world.Block;
import mindustryV4.world.meta.BlockStat;
import mindustryV4.world.meta.BlockStats;
import mindustryV4.world.meta.StatUnit;
import mindustryV4.world.meta.values.LiquidFilterValue;
import ucore.function.Predicate;
import ucore.scene.ui.layout.Table;

import static mindustryV4.Vars.content;

public class ConsumeLiquidFilter extends Consume{
    private final Predicate<Liquid> filter;
    private final float use;
    private final boolean isFuel;

    public ConsumeLiquidFilter(Predicate<Liquid> liquid, float amount, boolean isFuel){
        this.filter = liquid;
        this.use = amount;
        this.isFuel = isFuel;
    }

    public ConsumeLiquidFilter(Predicate<Liquid> liquid, float amount){
        this(liquid, amount, false);
    }

    @Override
    public void buildTooltip(Table table){
        Array<Liquid> list = new Array<>();

        for(Liquid item : content.liquids()){
            if(!item.isHidden() && filter.test(item)) list.add(item);
        }

        for(int i = 0; i < list.size; i++){
            Liquid item = list.get(i);
            table.addImage(item.getContentIcon()).size(8 * 3).padRight(2).padLeft(2).padTop(2).padBottom(2);
            if(i != list.size - 1){
                table.add("/");
            }
        }
    }

    @Override
    public String getIcon(){
        return "icon-liquid-small";
    }

    @Override
    public void update(Block block, TileEntity entity){
        entity.liquids.remove(entity.liquids.current(), use(block, entity));
    }

    @Override
    public boolean valid(Block block, TileEntity entity){
        return entity.liquids != null && filter.test(entity.liquids.current()) && entity.liquids.currentAmount() >= use(block, entity);
    }

    @Override
    public void display(BlockStats stats){
        if(optional){
            stats.add(BlockStat.boostLiquid, new LiquidFilterValue(filter));
        }else if(isFuel){
            stats.add(BlockStat.inputLiquidFuel, new LiquidFilterValue(filter));
            stats.add(BlockStat.liquidFuelUse, 60f * use, StatUnit.liquidSecond);
        }else {
            stats.add(BlockStat.inputLiquid, new LiquidFilterValue(filter));
            stats.add(BlockStat.liquidUse, 60f * use, StatUnit.liquidSecond);
        }
    }

    float use(Block block, TileEntity entity){
        return Math.min(use * entity.delta(), block.liquidCapacity);
    }
}
