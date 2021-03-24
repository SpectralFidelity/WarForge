package com.flansmod.warforge.common.blocks;

import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.WarForgeConfig;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class TileEntitySiegeCamp extends TileEntity implements IClaim
{
	private UUID mPlacer = Faction.NULL;
	private UUID mFactionUUID = Faction.NULL;
	private int mColour = 0xffffff;
	private String mFactionName = "";
	private BlockPos mSiegeTarget = null;
	
	public TileEntitySiegeCamp()
	{
		
	}
	
	public void OnPlacedBy(EntityLivingBase placer) 
	{
		mPlacer = placer.getUniqueID();
		
	}
	
	@Override
	public TileEntity GetAsTileEntity() { return this; }
	@Override
	public DimBlockPos GetPos() { return new DimBlockPos(world.provider.getDimension(), getPos()); }
	@Override
	public int GetDefenceStrength() { return 0; }
	@Override
	public int GetSupportStrength() { return 0; }
	@Override
	public int GetAttackStrength() { return WarForgeConfig.ATTACK_STRENGTH_SIEGE_CAMP; }
	@Override
	public UUID GetFaction() { return mFactionUUID; }
	@Override 
	public boolean CanBeSieged() { return false; }
	@Override
	public int GetColour() { return mColour; }
	@Override
	public String GetDisplayName() { return mFactionName; }

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		nbt.setUniqueId("placer", mPlacer);
		nbt.setUniqueId("faction", mFactionUUID);
		nbt.setBoolean("started", mSiegeTarget != null);
		if(mSiegeTarget != null)
		{
			nbt.setInteger("attackX", mSiegeTarget.getX());
			nbt.setInteger("attackY", mSiegeTarget.getY());
			nbt.setInteger("attackZ", mSiegeTarget.getZ());
		}
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		
		mPlacer = nbt.getUniqueId("placer");
		mFactionUUID = nbt.getUniqueId("faction");
		
		boolean started = nbt.getBoolean("started");
		if(started)
		{
			mSiegeTarget = new BlockPos(
					nbt.getInteger("attackX"),
					nbt.getInteger("attackY"),
					nbt.getInteger("attackZ"));
		}
		else mSiegeTarget = null;
		
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			Faction faction = WarForgeMod.FACTIONS.GetFaction(mFactionUUID);
			if(!mFactionUUID.equals(Faction.NULL) && faction == null)
			{
				WarForgeMod.LOGGER.error("Faction " + mFactionUUID + " could not be found for citadel at " + pos);
				//world.setBlockState(getPos(), Blocks.AIR.getDefaultState());
			}
			if(faction != null)
			{
				mColour = faction.mColour;
				mFactionName = faction.mName;
			}
		}
		else
		{
			WarForgeMod.LOGGER.error("Loaded TileEntity from NBT on client?");
		}
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
	}
	
	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, SPacketUpdateTileEntity packet)
	{
		NBTTagCompound tags = packet.getNbtCompound();
		
		mFactionUUID = tags.getUniqueId("faction");
		mColour = tags.getInteger("colour");
		mFactionName = tags.getString("name");
	}
	
	@Override
	public NBTTagCompound getUpdateTag()
	{
		// You have to get parent tags so that x, y, z are added.
		NBTTagCompound tags = super.getUpdateTag();

		// Custom partial nbt write method
		tags.setUniqueId("faction", mFactionUUID);
		tags.setInteger("colour", mColour);
		tags.setString("name", mFactionName);
		
		return tags;
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tags)
	{
		mFactionUUID = tags.getUniqueId("faction");
		mColour = tags.getInteger("colour");
		mFactionName = tags.getString("name");
	}

	@Override
	public void OnServerSetFaction(Faction faction) 
	{
		if(faction != null)
		{
			mFactionUUID = faction.mUUID;
			mColour = faction.mColour;
			mFactionName = faction.mName;
		}
		else
		{
			WarForgeMod.LOGGER.error("Siege camp placed by player with no faction");
		}
	}

	
	
}
