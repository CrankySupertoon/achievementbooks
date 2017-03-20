package com.stateshifterlabs.achievementbooks.networking;

import com.stateshifterlabs.achievementbooks.data.AchievementData;
import com.stateshifterlabs.achievementbooks.facade.BufferUtilities;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;


public class MigrationCompletionDetailsMessage implements IMessage {
	private final CompletionDetailsMessageBase base = new CompletionDetailsMessageBase(new BufferUtilities());

	public MigrationCompletionDetailsMessage withData(AchievementData achievementData) {
		base.withData(achievementData);
		return this;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		base.fromBytes(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		base.toBytes(buf);
	}

	public AchievementData achievementData() {
		return base.achievementData();
	}

}
