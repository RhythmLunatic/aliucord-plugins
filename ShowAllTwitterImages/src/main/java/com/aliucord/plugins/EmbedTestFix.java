package com.aliucord.plugins;

import android.content.Context;

import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.InsteadHook;
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


//TODO: Clean this up
import android.view.View;
import com.lytefast.flexinput.R;
import com.aliucord.patcher.Hook;
import com.discord.stores.StoreStream;
import android.widget.TextView;
import androidx.core.widget.NestedScrollView;
import com.discord.widgets.channels.list.WidgetChannelsListItemChannelActions;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import android.widget.LinearLayout;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import com.aliucord.Http;
import com.aliucord.Utils;
import com.aliucord.wrappers.embeds.MessageEmbedWrapper;
import com.aliucord.wrappers.embeds.VideoWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Needed for settings page
import com.discord.views.CheckedSetting;
import com.aliucord.api.SettingsAPI;
import com.aliucord.widgets.BottomSheet;
import android.os.Bundle;


// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class EmbedTestFix extends Plugin {


	public static class PluginSettings extends BottomSheet {
        private final SettingsAPI settings;

        public PluginSettings(SettingsAPI settings) { this.settings = settings; }

        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);

			//
            addView(createCheckedSetting(view.getContext(), "Enable video embeds (Kind of buggy, might show more than one embed)", "VideoEmbeds", true));
        }

        private CheckedSetting createCheckedSetting(Context ctx, String title, String setting, boolean checkedByDefault) {
        
        	//fun createCheckedSetting(context: Context, type: CheckedSetting.ViewType, text: CharSequence?, subtext: CharSequence?)
            CheckedSetting checkedSetting = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, null);

            checkedSetting.setChecked(settings.getBool(setting, checkedByDefault));
            checkedSetting.setOnCheckedListener( check -> {
                settings.setBool(setting, check);
            });

            return checkedSetting;
        }
    }
    
    public EmbedTestFix() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) {

    	Logger logger = new Logger("ShowAllTwitterImages");
		try {
			patcher.patch(
				ChatListEntry.Companion.class.getDeclaredMethod("createEmbedEntries",
					//Message message, StoreMessageState.State state, boolean isBlockedExpanded, boolean allowAnimatedEmojis, boolean autoPlayGifs, boolean dontDisplayAnyEmbeds, long guildID, boolean isThreadStarter
					Message.class, StoreMessageState.State.class, boolean.class, boolean.class, boolean.class, boolean.class, long.class, boolean.class),
				new Hook(callFrame->{
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
		
		if (settings.getBool("VideoEmbeds", true))
		{
			try {
				patcher.patch(WidgetChatListAdapterItemMessage.class.getDeclaredMethod("configureItemTag", Message.class),
					new Hook((cf)->{
						var msg =(Message) cf.args[0];
						var embeds = msg.getEmbeds();
						if (embeds.size() != 1)
							return;
						MessageEmbedWrapper twEmbed = new MessageEmbedWrapper(embeds.get(0));
						VideoWrapper v = twEmbed.getVideo();
						
						
						if (v!=null && v.getUrl().startsWith("https://twitter.com/i/videos"))
						{
							String[] newURL = twEmbed.getUrl().split("\\://",2);
							logger.debug(newURL[1]);
							new Thread(()->{
								
								try {
									Http.Request req = new Http.Request("https://fx"+newURL[1], "GET")
									.setHeader("User-Agent", "Mozilla/5.0 (compatible; Discordbot/2.0; +https://discordapp.com)")
									.setHeader("Accept", "*/*");
									Http.Response resp = req.execute();
									final String txt = resp.text();
									
									MessageEmbedBuilder embed = new MessageEmbedBuilder(EmbedType.VIDEO);
									
									final Pattern pattern = Pattern.compile("<meta (?:property=\\\"og:video\\\" *content=\\\"(.+?)\\\")");
									Matcher matcher = pattern.matcher(txt);
									
									if (matcher.find()) {
										logger.debug("Video: "+matcher.group(1));
										embed.setVideo(matcher.group(1));
									}
									else
									{
										Utils.showToast("ShowAllTwitterImages: No regex match for video!!");
										logger.debug(txt);
										//logger.debug("a");
									}
									
									final Pattern pattern2 = Pattern.compile("<meta (?:property=\\\"og:image\\\" *content=\\\"(.+?)\\\")");
									matcher = pattern2.matcher(txt);
									
									if (matcher.find()) {
										logger.debug("Thumb: "+matcher.group(1));
										embed.setThumbnail(matcher.group(1));
									}
									else
									{
										Utils.showToast("ShowAllTwitterImages: No regex match for thumbnail!!");
										logger.debug(txt);
										//logger.debug("a");
									}
									
									msg.getEmbeds().add(embed.build());
StoreStream.getMessages().handleMessageUpdate(msg.synthesizeApiMessage());

									
								}
								catch (Exception e) {
									Utils.showToast("Failed to fix twitter video embed, maybe fxtwitter is down?");
									e.printStackTrace();
								}
							}).start();
						}
						
					})
				);
			}
			catch (Exception e) {
				//Utils.showToast("Something wrong with video embeds feature...");
				e.printStackTrace();
			}
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
