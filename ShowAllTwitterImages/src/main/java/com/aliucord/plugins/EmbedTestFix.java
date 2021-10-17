package com.aliucord.plugins;

import android.content.Context;

import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.patcher.PineInsteadFn;
import com.aliucord.wrappers.embeds.MessageEmbedWrapper;
import com.aliucord.Logger;
import com.aliucord.utils.ReflectUtils;

import com.discord.api.message.embed.EmbedType;
import com.discord.models.message.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.discord.api.message.embed.MessageEmbed;
import com.discord.stores.StoreMessageState;
import com.discord.widgets.chat.list.entries.ChatListEntry;
import com.discord.widgets.chat.list.entries.MessageEntry;
import com.discord.widgets.chat.list.entries.EmbedEntry;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class EmbedTestFix extends Plugin {
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) {

    	//Logger logger = new Logger("TwitterShowAllImages");
		try {
			patcher.patch(
				ChatListEntry.Companion.class.getDeclaredMethod("createEmbedEntries",
					//Message message, StoreMessageState.State state, boolean isBlockedExpanded, boolean allowAnimatedEmojis, boolean autoPlayGifs, boolean dontDisplayAnyEmbeds, long guildID, boolean isThreadStarter
					Message.class, StoreMessageState.State.class, boolean.class, boolean.class, boolean.class, boolean.class, long.class, boolean.class),
				new PinePatchFn(callFrame->{
					Message message = (Message)callFrame.args[0];
					long guildID = (long)callFrame.args[6];

					List embedAndAttachmentList = (List)callFrame.getResult();
					List<MessageEmbed> embeds = message.getEmbeds();

					if (embedAndAttachmentList.size() < 1 || embeds.size() < 2 || embedAndAttachmentList.size() == embeds.size())
						return;

					EmbedEntry lastAddedEmbed = (EmbedEntry)embedAndAttachmentList.get(embedAndAttachmentList.size()-1);
					int embedIndex = lastAddedEmbed.component1()+1;

					//logger.verbose("Got "+String.valueOf(embedAndAttachmentList.size())+" objects in the render list and "+String.valueOf(embeds.size())+ " embeds. ");
					//Throws "Attempt to invoke virtual method 'int java.lang.Integer.intValue()' on a null object reference" if I don't do this
					var embedColor = embeds.get(0).b() != null ? embeds.get(0).b() : null;
					for (int i = 1; i < embeds.size(); i++)
					{
						var embed = embeds.get(i);
						if (embedColor != null)
						{
							try {
								ReflectUtils.setField(embed,"color",embedColor);
							}
							catch (Exception e) {
								//do nothing
							}
						}

						//int embedIndex, long guildId, Message message2, StoreMessageState.State state, MessageEmbed messageEmbed, boolean isBlockedExpanded, boolean allowAnimatedEmojis, boolean autoplayGifs, boolean isThreadStarter
						embedAndAttachmentList.add(new EmbedEntry(
								embedIndex,
								guildID,
								message,
								(StoreMessageState.State)callFrame.args[1],
								embed,
								(boolean)callFrame.args[2],
								(boolean)callFrame.args[3],
								(boolean)callFrame.args[4],
								(boolean)callFrame.args[7])
						);
						//logger.verbose("Added new embed to arrayList at position "+String.valueOf(embedIndex));
						embedIndex++;
					}
				})
			);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        // Remove all patches
        patcher.unpatchAll();
    }
}

/*
Copyright (C) Rhythm Lunatic 2021

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
