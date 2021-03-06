package de.mennomax.astikorcarts;

import de.mennomax.astikorcarts.client.ClientInitializer;
import de.mennomax.astikorcarts.entity.SupplyCartEntity;
import de.mennomax.astikorcarts.entity.AnimalCartEntity;
import de.mennomax.astikorcarts.entity.PlowEntity;
import de.mennomax.astikorcarts.entity.PostilionEntity;
import de.mennomax.astikorcarts.inventory.container.PlowContainer;
import de.mennomax.astikorcarts.item.CartItem;
import de.mennomax.astikorcarts.network.NetBuilder;
import de.mennomax.astikorcarts.network.serverbound.ActionKeyMessage;
import de.mennomax.astikorcarts.network.serverbound.OpenSupplyCartMessage;
import de.mennomax.astikorcarts.network.serverbound.ToggleSlowMessage;
import de.mennomax.astikorcarts.network.clientbound.UpdateDrawnMessage;
import de.mennomax.astikorcarts.server.ServerInitializer;
import de.mennomax.astikorcarts.util.DefRegister;
import de.mennomax.astikorcarts.util.RegObject;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.stats.IStatFormatter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

@Mod(AstikorCarts.ID)
public final class AstikorCarts {
    public static final String ID = "astikorcarts";

    public static final SimpleChannel CHANNEL = new NetBuilder(new ResourceLocation(ID, "main"))
        .version(1).optionalServer().requiredClient()
        .serverbound(ActionKeyMessage::new).consumer(() -> ActionKeyMessage::handle)
        .serverbound(ToggleSlowMessage::new).consumer(() -> ToggleSlowMessage::handle)
        .clientbound(UpdateDrawnMessage::new).consumer(() -> new UpdateDrawnMessage.Handler())
        .serverbound(OpenSupplyCartMessage::new).consumer(() -> OpenSupplyCartMessage::handle)
        .build();

    private static final DefRegister REG = new DefRegister(ID);

    public static final class Items {
        private Items() {
        }

        private static final DefRegister.Forge<Item> R = REG.of(ForgeRegistries.ITEMS);

        public static final RegObject<Item, Item> WHEEL, SUPPLY_CART, PLOW, ANIMAL_CART;

        static {
            WHEEL = R.make("wheel", () -> new Item(new Item.Properties().group(ItemGroup.MATERIALS)));
            final Supplier<Item> cart = () -> new CartItem(new Item.Properties().maxStackSize(1).group(ItemGroup.TRANSPORTATION));
            SUPPLY_CART = R.make("supply_cart", cart);
            PLOW = R.make("plow", cart);
            ANIMAL_CART = R.make("animal_cart", cart);
        }
    }

    public static final class EntityTypes {
        private EntityTypes() {
        }

        private static final DefRegister.Forge<EntityType<?>> R = REG.of(ForgeRegistries.ENTITIES);

        public static final RegObject<EntityType<?>, EntityType<SupplyCartEntity>> SUPPLY_CART;
        public static final RegObject<EntityType<?>, EntityType<PlowEntity>> PLOW;
        public static final RegObject<EntityType<?>, EntityType<AnimalCartEntity>> ANIMAL_CART;
        public static final RegObject<EntityType<?>, EntityType<PostilionEntity>> POSTILION;

        static {
            SUPPLY_CART = R.make("supply_cart", () -> EntityType.Builder.create(SupplyCartEntity::new, EntityClassification.MISC)
                .size(1.5F, 1.4F)
                .build(ID + ":supply_cart"));
            PLOW = R.make("plow", () -> EntityType.Builder.create(PlowEntity::new, EntityClassification.MISC)
                .size(1.3F, 1.4F)
                .build(ID + ":plow"));
            ANIMAL_CART = R.make("animal_cart", () -> EntityType.Builder.create(AnimalCartEntity::new, EntityClassification.MISC)
                .size(1.3F, 1.4F)
                .build(ID + ":animal_cart"));
            POSTILION = R.make("postilion", () -> EntityType.Builder.create(PostilionEntity::new, EntityClassification.MISC)
                .size(0.25F, 0.25F)
                .disableSummoning()
                .disableSerialization()
                .build(ID + ":postilion"));
        }
    }

    public static final class SoundEvents {
        private SoundEvents() {
        }

        private static final DefRegister.Forge<SoundEvent> R = REG.of(ForgeRegistries.SOUND_EVENTS);

        public static final RegObject<SoundEvent, SoundEvent> CART_ATTACHED = R.make("entity.cart.attach", SoundEvent::new);
        public static final RegObject<SoundEvent, SoundEvent> CART_DETACHED = R.make("entity.cart.detach", SoundEvent::new);
        public static final RegObject<SoundEvent, SoundEvent> CART_PLACED = R.make("entity.cart.place", SoundEvent::new);
    }

    public static final class Stats {
        private Stats() {
        }

        private static final DefRegister.Vanilla<ResourceLocation, IStatFormatter> R = REG.of(Registry.CUSTOM_STAT, net.minecraft.stats.Stats.CUSTOM::get, rl -> IStatFormatter.DEFAULT);

        public static final ResourceLocation CART_ONE_CM = R.make("cart_one_cm", rl -> rl, rl -> IStatFormatter.DISTANCE);
    }

    public static final class ContainerTypes {
        private ContainerTypes() {
        }

        private static final DefRegister.Forge<ContainerType<?>> R = REG.of(ForgeRegistries.CONTAINERS);

        public static final RegObject<ContainerType<?>, ContainerType<PlowContainer>> PLOW_CART = R.make("plow", () -> IForgeContainerType.create(PlowContainer::new));
    }

    public AstikorCarts() {
        final Initializer.Context ctx = new InitContext();
        DistExecutor.runForDist(() -> ClientInitializer::new, () -> ServerInitializer::new).init(ctx);
        REG.registerAll(ctx.modBus(), Items.R, EntityTypes.R, SoundEvents.R, ContainerTypes.R, Stats.R);
    }

    private static class InitContext implements Initializer.Context {
        @Override
        public ModLoadingContext context() {
            return ModLoadingContext.get();
        }

        @Override
        public IEventBus bus() {
            return MinecraftForge.EVENT_BUS;
        }

        @Override
        public IEventBus modBus() {
            return FMLJavaModLoadingContext.get().getModEventBus();
        }
    }
}
