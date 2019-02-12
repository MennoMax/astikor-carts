package de.mennomax.astikoorcarts.config;

import de.mennomax.astikoorcarts.AstikoorCarts;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = AstikoorCarts.MODID)
public class ModConfig
{
    @Config.LangKey("entity.astikoorcarts:cargocart.name")
    public static final CargoCart cargocart = new CargoCart();

    @Config.LangKey("entity.astikoorcarts:plowcart.name")
    public static final PlowCart plowcart = new PlowCart();

    public static class CargoCart
    {
        public String[] canPull = {
                "minecraft:horse",
                "minecraft:donkey",
                "minecraft:mule",
                "minecraft:pig"
        };
    }

    public static class PlowCart
    {
        public String[] canPull = {
                "minecraft:horse",
                "minecraft:donkey",
                "minecraft:mule",
                "minecraft:pig"
        };
    }

    @Mod.EventBusSubscriber
    private static class EventHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(AstikoorCarts.MODID))
            {
                ConfigManager.sync(AstikoorCarts.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
