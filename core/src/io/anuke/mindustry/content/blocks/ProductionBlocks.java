package io.anuke.mindustry.content.blocks;

import com.badlogic.gdx.graphics.Color;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.Liquids;
import io.anuke.mindustry.content.fx.BlockFx;
import io.anuke.mindustry.type.ContentList;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.blocks.production.Cultivator;
import io.anuke.mindustry.world.blocks.production.Drill;
import io.anuke.mindustry.world.blocks.production.Fracker;
import io.anuke.mindustry.world.blocks.production.SolidPump;

public class ProductionBlocks extends BlockList implements ContentList{
    public static Block tungstenDrill, carbideDrill, laserdrill, blastdrill, plasmadrill, waterextractor, oilextractor, cultivator;

    @Override
    public void load(){
        tungstenDrill = new Drill("tungsten-drill"){{
            tier = 2;
            drillTime = 340;
        }};

        carbideDrill = new Drill("carbide-drill"){{
            tier = 3;
            drillTime = 280;
        }};

        laserdrill = new Drill("laser-drill"){{
            drillTime = 180;
            size = 2;
            hasPower = true;
            tier = 4;
            updateEffect = BlockFx.pulverizeMedium;
            drillEffect = BlockFx.mineBig;

            consumes.power(0.2f);
        }};

        blastdrill = new Drill("blast-drill"){{
            drillTime = 120;
            size = 3;
            drawRim = true;
            hasPower = true;
            tier = 5;
            updateEffect = BlockFx.pulverizeRed;
            updateEffectChance = 0.03f;
            drillEffect = BlockFx.mineHuge;
            rotateSpeed = 6f;
            warmupSpeed = 0.01f;

            consumes.power(0.5f);
        }};

        plasmadrill = new Drill("plasma-drill"){{
            heatColor = Color.valueOf("ff461b");
            drillTime = 90;
            size = 4;
            hasLiquids = true;
            hasPower = true;
            tier = 5;
            rotateSpeed = 9f;
            drawRim = true;
            updateEffect = BlockFx.pulverizeRedder;
            updateEffectChance = 0.04f;
            drillEffect = BlockFx.mineHuge;
            warmupSpeed = 0.005f;

            consumes.power(0.7f);
        }};

        waterextractor = new SolidPump("water-extractor"){{
            result = Liquids.water;
            pumpAmount = 0.1f;
            size = 2;
            liquidCapacity = 30f;
            rotateSpeed = 1.4f;

            consumes.power(0.2f);
        }};

        oilextractor = new Fracker("oil-extractor"){{
            result = Liquids.oil;
            updateEffect = BlockFx.pulverize;
            liquidCapacity = 50f;
            updateEffectChance = 0.05f;
            pumpAmount = 0.08f;
            size = 3;
            liquidCapacity = 30f;

            consumes.item(Items.sand);
            consumes.power(0.5f);
            consumes.liquid(Liquids.water, 0.3f);
        }};

        cultivator = new Cultivator("cultivator"){{
            result = Items.biomatter;
            drillTime = 260;
            size = 2;
            hasLiquids = true;
            hasPower = true;

            consumes.power(0.08f);
            consumes.liquid(Liquids.water, 0.2f);
        }};

    }
}