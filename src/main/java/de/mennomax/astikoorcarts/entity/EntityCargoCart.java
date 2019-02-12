package de.mennomax.astikoorcarts.entity;

import de.mennomax.astikoorcarts.AstikoorCarts;
import de.mennomax.astikoorcarts.config.ModConfig;
import de.mennomax.astikoorcarts.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityCargoCart extends AbstractDrawnInventory implements IInventoryChangedListener
{
    private static final DataParameter<Integer> CARGO = EntityDataManager.<Integer>createKey(EntityCargoCart.class, DataSerializers.VARINT);

    public EntityCargoCart(World worldIn)
    {
        super(worldIn);
        this.setSize(1.5F, 1.4F);
        this.stepHeight = 1.2F;
        this.offsetFactor = 2.4D;
        this.inventory = new InventoryBasic(this.getName(), true, 54);
        this.inventory.addInventoryChangeListener(this);
    }

    @Override
    public boolean canPull(Entity pullingIn)
    {
        for (String entry : ModConfig.cargocart.canPull)
        {
            if (entry.equals(pullingIn instanceof EntityPlayer ? "minecraft:player" : EntityList.getKey(pullingIn).toString()))
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Item getCartItem()
    {
        return ModItems.CARGOCART;
    }
    
    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
    {
        {
            if (!this.world.isRemote)
            {
                if (player.isSneaking())
                {
                    player.openGui(AstikoorCarts.instance, 0, this.world, this.getEntityId(), 0, 0);
                }
                else
                {
                    player.startRiding(this);
                }

            }
        }
        return true;
    }

    @Override
    public double getMountedYOffset()
    {
        return 0.62D;
    }

    @Override
    public void updatePassenger(Entity passenger)
    {
        if (this.isPassenger(passenger))
        {
            Vec3d vec3d = (new Vec3d((double) -0.68D, 0.0D, 0.0D)).rotateYaw(-this.rotationYaw * 0.017453292F - ((float) Math.PI / 2F));
            passenger.setPosition(this.posX + vec3d.x, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ + vec3d.z);
        }
    }
    
    public int getCargo()
    {
        return this.dataManager.get(CARGO);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        dataManager.set(CARGO, compound.getInteger("Cargo"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setInteger("Cargo", dataManager.get(CARGO));
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(CARGO, 0);
    }

    @Override
    public void onInventoryChanged(IInventory invBasic)
    {
        if (!this.world.isRemote)
        {
            int tempload = 0;
            for (int i = 0; i < this.inventory.getSizeInventory(); i++)
            {
                if (!this.inventory.getStackInSlot(i).isEmpty())
                {
                    tempload++;
                }
            }
            int newValue;
            if (tempload > 31)
                newValue = 4;
            else if (tempload > 16)
                newValue = 3;
            else if (tempload > 8)
                newValue = 2;
            else if (tempload > 3)
                newValue = 1;
            else
                newValue = 0;
            if (this.dataManager.get(CARGO).intValue() != newValue)
            {
                this.dataManager.set(CARGO, newValue);
            }
        }
    }

}
