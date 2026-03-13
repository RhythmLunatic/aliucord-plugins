package com.accelerator.plugins;

import android.content.Context;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;

import com.aliucord.Logger;
import com.aliucord.Utils;

import com.aliucord.utils.ReflectUtils;
import com.discord.widgets.chat.input.WidgetChatInputAttachments;
import com.discord.widgets.chat.input.WidgetChatInputAttachments$createAndConfigureExpressionFragment$stickerPickerListener$1;
import com.discord.widgets.chat.input.sticker.*;
import com.discord.utilities.stickers.StickerUtils;
import com.discord.widgets.chat.MessageManager;
import java.util.Collections;
import com.discord.stores.StoreStream;
//import org.json.JSONObject;
//import com.discord.utilities.analytics.AnalyticSuperProperties;
//import com.aliucord.Http;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class FakeStickers extends Plugin {

    public FakeStickers() {}

	@Override
	// Called when your plugin is started. This is the place to register command, add patches, etc
	public void start(Context context) throws Throwable {
		var fMessageManager = StickerPickerViewModel.class.getDeclaredField("messageManager");
		fMessageManager.setAccessible(true);
		// add the patch

		// Do not mark stickers as unsendable (grey overlay)
		patcher.patch(StickerItem.class.getDeclaredMethod("getSendability"), InsteadHook.returnConstant(StickerUtils.StickerSendability.SENDABLE));

		//Patch onClick to send sticker
		patcher.patch(WidgetStickerPicker.class.getDeclaredMethod("onStickerItemSelected", StickerItem.class), new PreHook(param -> {
			try {
				// getSendability is patched above to always return SENDABLE so get the real value via reflect
				if (ReflectUtils.getField(param.args[0], "sendability") == StickerUtils.StickerSendability.SENDABLE) return;

				var sticker = ((StickerItem) param.args[0]).getSticker();
				var messageManager = (MessageManager) fMessageManager.get(
					WidgetStickerPicker.access$getViewModel$p((WidgetStickerPicker) param.thisObject)
				);
				messageManager.sendMessage(
					"https://media.discordapp.net/stickers/"+sticker.d/*id*/()+sticker.b/*format*/()+"?size=160",
					Collections.emptyList(),//mentions
					null,//attachments
					null,//channel ID
					Collections.emptyList(),//stickers
					true,//use pending reply
					null,//message too long handler(?)
					null,//attachments too large handler(?)
					(m) -> {
						this.logger.debug(m.getMessageResult().toString());
						return null;
					}//messageSendResultHandler
				);

				// Skip original method
				param.setResult(null);

				// Dismiss sticker picker
				var stickerListener = (WidgetChatInputAttachments$createAndConfigureExpressionFragment$stickerPickerListener$1) // What a classname jeez
						ReflectUtils.getField(param.thisObject, "stickerPickerListener");
				//.s here is FlexInputFragment's FlexInputViewModel property (obfuscated to s)
				WidgetChatInputAttachments.access$getFlexInputFragment$p(stickerListener.this$0).s.hideExpressionTray();
			} catch (Throwable e) {
				this.logger.error(e);
			}
		}));
	}

	@Override
	// Called when your plugin is stopped
	public void stop(Context context) {
		// Remove all patches
		patcher.unpatchAll();
	}
}

/*
This plugin was mainly written as a learning exercise in order to
figure out how to send links on tap since it would be required to
send custom stickers. Whether or not custom stickers will ever
happen remains to be seen...


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
