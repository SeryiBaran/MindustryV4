package mindustryV4.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;
import ucore.util.Strings;

import java.io.IOException;

public class Version{
    /**Build type. 'official' for official releases; 'custom' or 'bleeding edge' are also used.*/
    public static String type;
    /**Build modifier, e.g. 'alpha' or 'release'*/
    public static String modifier;
    /**Number specifying the major version, e.g. '4'*/
    public static int number;
    /**Build number, e.g. '43'. set to '-1' for custom builds.*/
    public static int build = 0;
    /**Revision number. Used for hotfixes. Does not affect server compatibility.*/
    public static int revision = 0;

    public static void init(){
        try{
            FileHandle file = Gdx.files.internal("version.properties");

            ObjectMap<String, String> map = new ObjectMap<>();
            PropertiesUtils.load(map, file.reader());

            type = map.get("type");
            number = Integer.parseInt(map.get("number"));
            modifier = map.get("modifier");
            if(map.get("build").contains(".")){
                String[] split = map.get("build").split("\\.");
                try{
                    build = Integer.parseInt(split[0]);
                    revision = Integer.parseInt(split[1]);
                }catch(Throwable e){
                    e.printStackTrace();
                    build = -1;
                }
            }else{
                build = Strings.canParseInt(map.get("build")) ? Integer.parseInt(map.get("build")) : -1;
            }
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
