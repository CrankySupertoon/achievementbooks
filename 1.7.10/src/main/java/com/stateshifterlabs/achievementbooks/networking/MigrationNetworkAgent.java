package com.stateshifterlabs.achievementbooks.networking;

import com.stateshifterlabs.achievementbooks.data.AchievementData;
import com.stateshifterlabs.achievementbooks.data.AchievementStorage;
import com.stateshifterlabs.achievementbooks.data.Book;
import com.stateshifterlabs.achievementbooks.data.compatibility.SA.SA;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.File;

import static com.stateshifterlabs.achievementbooks.AchievementBooksMod.MODID;

public class MigrationNetworkAgent
{

	private SimpleNetworkWrapper wrapper;
	private AchievementStorage storage;
	private Book targetBook;
	private NetworkAgent networkAgent;
	private File configDir;
	private int packetId = 10000;
	private int delay = 160;
	private int delayServer = 160;
	private final SA importer;

	public MigrationNetworkAgent(Book targetBook, NetworkAgent networkAgent, File configDir) {
		importer = new SA(configDir.getAbsolutePath());
		this.storage = new AchievementStorage();
		this.targetBook = targetBook;
		this.networkAgent = networkAgent;
		this.configDir = configDir;
		String channelName = String.format("mezamigr");
		wrapper = new SimpleNetworkWrapper(channelName);
		wrapper.registerMessage(new MigrationClientHandler(storage), MigrationCompletionDetailsMessage.class, packetId++, Side.CLIENT);
	}


	@SubscribeEvent
	@SideOnly(Side.SERVER)
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;
		if (player != null && !player.worldObj.isRemote) {
			AchievementData data = importer.getUserSave(player.getDisplayName(), targetBook);
			sendMigrationCompletedAchievements((EntityPlayerMP) player, data);
		}

	}

	private void sendMigrationCompletedAchievements(EntityPlayerMP player, AchievementData data) {
		MigrationCompletionDetailsMessage completionDetailsMessage = new MigrationCompletionDetailsMessage();
		completionDetailsMessage.withData(data);
		wrapper.sendTo(completionDetailsMessage, player);
	}


	@SubscribeEvent
	@SideOnly(Side.SERVER)
	public void onServerPlayerTick(TickEvent.PlayerTickEvent event) {
		delayServer--;
		if(delayServer>1) {
			return;
		}
		delayServer = 160;
		EntityPlayer player = event.player;

		Item SAbook = (Item) Item.itemRegistry.getObject("SimpleAchievements:sa.achievementBook");
		final InventoryPlayer inventory = player.inventory;

		updatePlayerInventory(event, SAbook, inventory, importer.getUserSave(event.player.getDisplayName(), targetBook));

	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		delay--;
		if(delay>1) {
			return;
		}
		delay = 160;
		Item SAbook = (Item) Item.itemRegistry.getObject("SimpleAchievements:sa.achievementBook");
		final InventoryPlayer inventory = event.player.inventory;

		if(!event.player.getEntityWorld().isRemote) {
			updatePlayerInventory(event, SAbook, inventory, storage.forPlayer(event.player.getDisplayName()));
		}
	}

	private void updatePlayerInventory(TickEvent.PlayerTickEvent event, Item SAbook, InventoryPlayer inventory, AchievementData data) {
		if(inventory.hasItem(SAbook)) {

			Item theBook = (Item) Item.itemRegistry.getObject(MODID+":"+targetBook.itemName());

			int inventorySize = inventory.getSizeInventory();

			for(int i = 0; i<inventorySize; i++ ) {
				ItemStack stack = inventory.getStackInSlot(i);
				if (stack == null) {
					continue;
				}
				if (stack.getItem() == SAbook) {
					inventory.setInventorySlotContents(i, (ItemStack) null);
					ItemStack newBook = new ItemStack(theBook, 1);
					inventory.setInventorySlotContents(i, newBook);
				}
			}


		}
	}

}
