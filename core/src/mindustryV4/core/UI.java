package mindustryV4.core;

import io.anuke.arc.ApplicationListener;
import io.anuke.arc.Core;
import io.anuke.arc.Events;
import io.anuke.arc.Graphics.Cursor;
import io.anuke.arc.Graphics.Cursor.SystemCursor;
import io.anuke.arc.freetype.FreeTypeFontGenerator;
import io.anuke.arc.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.Colors;
import io.anuke.arc.graphics.g2d.BitmapFont;
import io.anuke.arc.input.KeyCode;
import io.anuke.arc.math.Interpolation;
import io.anuke.arc.scene.Group;
import io.anuke.arc.scene.Scene;
import io.anuke.arc.scene.Skin;
import io.anuke.arc.scene.actions.Actions;
import io.anuke.arc.scene.ui.Dialog;
import io.anuke.arc.scene.ui.TextField;
import io.anuke.arc.scene.ui.TextField.TextFieldFilter;
import io.anuke.arc.scene.ui.TooltipManager;
import io.anuke.arc.scene.ui.layout.Table;
import io.anuke.arc.scene.ui.layout.Unit;
import io.anuke.arc.util.Align;
import io.anuke.arc.util.Strings;
import io.anuke.arc.util.Time;
import mindustryV4.editor.MapEditorDialog;
import mindustryV4.game.EventType.ResizeEvent;
import mindustryV4.graphics.Pal;
import mindustryV4.ui.dialogs.*;
import mindustryV4.ui.fragments.*;

import static io.anuke.arc.scene.actions.Actions.*;
import static mindustryV4.Vars.control;
import static mindustryV4.Vars.disableUI;

public class UI implements ApplicationListener{
    private FreeTypeFontGenerator generator;

    public MenuFragment menufrag;
    public HudFragment hudfrag;
    public ChatFragment chatfrag;
    public PlayerListFragment listfrag;
    public BackgroundFragment backfrag;
    public LoadingFragment loadfrag;

    public AboutDialog about;
    public GameOverDialog restart;
    public CustomGameDialog custom;
    public MapsDialog maps;
    public LoadDialog load;
    public TelegramDialog telegram;
    public JoinDialog join;
    public HostDialog host;
    public PausedDialog paused;
    public SettingsMenuDialog settings;
    public ControlsDialog controls;
    public MapEditorDialog editor;
    public LanguageDialog language;
    public BansDialog bans;
    public AdminsDialog admins;
    public TraceDialog traces;
    public ChangelogDialog changelog;
    public LocalPlayerDialog localplayers;
    public DatabaseDialog database;
    public ContentInfoDialog content;
    public SectorsDialog sectors;
    public TechTreeDialog tech;

    public Cursor drillCursor, unloadCursor;

    public UI(){
        Skin skin = new Skin(Core.atlas);
        generateFonts(skin);
        skin.load(Core.files.internal("sprites/uiskin.json"));

        for(BitmapFont font : skin.getAll(BitmapFont.class).values()){
            font.setUseIntegerPositions(true);
        }

        Core.scene = new Scene(skin);
        Core.input.addProcessor(Core.scene);

        Dialog.setShowAction(() -> sequence(
            alpha(0f),
            originCenter(),
            moveToAligned(Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f, Align.center),
            scaleTo(0.0f, 1f),
            parallel(
                scaleTo(1f, 1f, 0.1f, Interpolation.fade),
                fadeIn(0.1f, Interpolation.fade)
            )
        ));

        Dialog.setHideAction(() -> sequence(
            parallel(
                scaleTo(0.01f, 0.01f, 0.1f, Interpolation.fade),
                fadeOut(0.1f, Interpolation.fade)
            )
        ));

        TooltipManager.getInstance().animations = false;

        Core.settings.setErrorHandler(e -> {
            e.printStackTrace();
            Core.app.post(() -> showError("Failed to access local storage.\nSettings will not be saved."));
        });

        Colors.put("accent", Pal.accent);

        loadCursors();
    }

    void loadCursors(){
        int cursorScaling = 3;
        Color outlineColor = Color.valueOf("444444");

        drillCursor = Core.graphics.newCursor("drill", cursorScaling, outlineColor);
        unloadCursor = Core.graphics.newCursor("unload", cursorScaling, outlineColor);
        SystemCursor.arrow.set(Core.graphics.newCursor("cursor", cursorScaling, outlineColor));
        SystemCursor.hand.set(Core.graphics.newCursor("hand", cursorScaling, outlineColor));
        SystemCursor.ibeam.set(Core.graphics.newCursor("ibeam", cursorScaling, outlineColor));

        Core.graphics.restoreCursor();
    }

    void generateFonts(Skin skin){
        generator = new FreeTypeFontGenerator(Core.files.internal("fonts/pixel.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = (int)(14*2 * Math.max(Unit.dp.scl(1f), 0.5f));
        param.shadowColor = Color.DARK_GRAY;
        param.shadowOffsetY = 2;
        param.incremental = true;

        skin.add("default-font", generator.generateFont(param));
        skin.add("default-font-chat", generator.generateFont(param));
        skin.getFont("default-font").getData().markupEnabled = true;
        skin.getFont("default-font").setOwnsTexture(false);
    }

    @Override
    public void update(){
        if(disableUI) return;

        Core.scene.act();
        Core.scene.draw();
    }

    @Override
    public void init(){
        menufrag = new MenuFragment();
        hudfrag = new HudFragment();
        chatfrag = new ChatFragment();
        listfrag = new PlayerListFragment();
        backfrag = new BackgroundFragment();
        loadfrag = new LoadingFragment();

        editor = new MapEditorDialog();
        controls = new ControlsDialog();
        restart = new GameOverDialog();
        join = new JoinDialog();
        telegram = new TelegramDialog();
        load = new LoadDialog();
        custom = new CustomGameDialog();
        language = new LanguageDialog();
        database = new DatabaseDialog();
        settings = new SettingsMenuDialog();
        host = new HostDialog();
        paused = new PausedDialog();
        changelog = new ChangelogDialog();
        about = new AboutDialog();
        bans = new BansDialog();
        admins = new AdminsDialog();
        traces = new TraceDialog();
        maps = new MapsDialog();
        localplayers = new LocalPlayerDialog();
        content = new ContentInfoDialog();
        sectors = new SectorsDialog();
        tech = new TechTreeDialog();

        Group group = Core.scene.root;

        backfrag.build(group);
        control.input(0).getFrag().build(group);
        hudfrag.build(group);
        menufrag.build(group);
        chatfrag.container().build(group);
        listfrag.build(group);
        loadfrag.build(group);
    }

    @Override
    public void resize(int width, int height){
        Core.scene.resize(width, height);
        Events.fire(new ResizeEvent());
    }

    @Override
    public void dispose(){
        generator.dispose();
    }

    public void loadAnd(Runnable call){
        loadAnd("$loading", call);
    }

    public void loadAnd(String text, Runnable call){
        loadfrag.show(text);
        Time.runTask(7f, () -> {
            call.run();
            loadfrag.hide();
        });
    }

    public void showTextInput(String titleText, String text, String def, TextFieldFilter filter, Consumer<String> confirmed){
        new Dialog(titleText, "dialog"){{
            cont.margin(30).add(text).padRight(6f);
            TextField field = cont.addField(def, t -> {
            }).size(170f, 50f).get();
            field.setTextFieldFilter((f, c) -> field.getText().length() < 12 && filter.acceptChar(f, c));
            Platform.instance.addDialog(field);
            buttons.defaults().size(120, 54).pad(4);
            buttons.addButton("$ok", () -> {
                confirmed.accept(field.getText());
                hide();
            }).disabled(b -> field.getText().isEmpty());
            buttons.addButton("$cancel", this::hide);
        }}.show();
    }

    public void showTextInput(String title, String text, String def, Consumer<String> confirmed){
        showTextInput(title, text, def, (field, c) -> true, confirmed);
    }

    public void showInfoFade(String info){
        Table table = new Table();
        table.setFillParent(true);
        table.actions(Actions.fadeOut(7f, Interpolation.fade), Actions.removeActor());
        table.top().add(info).padTop(40);
        Core.scene.add(table);
    }

    public void showInfo(String info){
        new Dialog("", "dialog"){{
            getCell(cont).growX();
            cont.margin(15).add(info).width(400f).wrap().get().setAlignment(Align.center, Align.center);
            buttons.addButton("$ok", this::hide).size(90, 50).pad(4);
        }}.show();
    }

    public void showInfoText(String titleText, String text){
        new Dialog(titleText, "dialog"){{
            cont.margin(15).add(text).width(400f).wrap().left().get().setAlignment(Align.left, Align.left);
            buttons.addButton("$ok", this::hide).size(90, 50).pad(4);
        }}.show();
    }

    public void showError(String text){
        new Dialog("$error.title", "dialog"){{
            cont.margin(15).add(text).width(400f).wrap().get().setAlignment(Align.center, Align.center);
            buttons.addButton("$ok", this::hide).size(90, 50).pad(4);
        }}.show();
    }

    public void showText(String titleText, String text){
        new Dialog(titleText, "dialog"){{
            cont.margin(15).add(text).width(400f).wrap().get().setAlignment(Align.center, Align.center);
            buttons.addButton("$ok", this::hide).size(90, 50).pad(4);
        }}.show();
    }

    public void showConfirm(String title, String text, Runnable confirmed){
        FloatingDialog dialog = new FloatingDialog(title);
        dialog.cont.add(text).width(500f).wrap().pad(4f).get().setAlignment(Align.center, Align.center);
        dialog.buttons.defaults().size(200f, 54f).pad(2f);
        dialog.setFillParent(false);
        dialog.buttons.addButton("$cancel", dialog::hide);
        dialog.buttons.addButton("$ok", () -> {
            dialog.hide();
            confirmed.run();
        });
        dialog.keyDown(KeyCode.ESCAPE, dialog::hide);
        dialog.keyDown(KeyCode.BACK, dialog::hide);
        dialog.show();
    }

    public String formatAmount(int number){
        if(number >= 1000000){
            return Strings.toFixed(number / 1000000f, 1) + "[gray]mil[]";
        }else if(number >= 10000){
            return number / 1000 + "[gray]k[]";
        }else if(number >= 1000){
            return Strings.toFixed(number / 1000f, 1) + "[gray]k[]";
        }else{
            return number + "";
        }
    }
}
