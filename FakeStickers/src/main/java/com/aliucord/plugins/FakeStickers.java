package com.aliucord.plugins;

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
import com.discord.utilities.rest.RestAPI;
//import com.discord.restapi.*;
import com.discord.restapi.RestAPIParams;
import com.discord.models.domain.NonceGenerator;
import com.discord.utilities.time.ClockFactory;
import com.aliucord.utils.RxUtils;
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
		// add the patch
		
		/*patcher.patch("WidgetChatInputAttachments$createAndConfigureExpressionFragment$emojiPickerListener$1",
			"WidgetChatInputAttachments$createAndConfigureExpressionFragment$emojiPickerListener$1",
			new Class<?>[] { WidgetChatInputAttachments.class }, new PinePatchFn( callFrame -> {
			
		}))*/

		// Do not mark stickers as unsendable (grey overlay)
		patcher.patch(StickerItem.class.getDeclaredMethod("getSendability"), InsteadHook.returnConstant(StickerUtils.StickerSendability.SENDABLE));

		patcher.patch(WidgetStickerPicker.class.getDeclaredMethod("onStickerItemSelected", StickerItem.class), new PreHook(param -> {
			try {
				// getSendability is patched above to always return SENDABLE so get the real value via reflect
				if (ReflectUtils.getField(param.args[0], "sendability") == StickerUtils.StickerSendability.SENDABLE) return;

				var sticker = ((StickerItem) param.args[0]).getSticker();

				RestAPIParams.Message message = new RestAPIParams.Message(
					"https://cdn.discordapp.com/stickers/"+sticker.d()+sticker.b(),
					Long.toString(NonceGenerator.computeNonce(ClockFactory.get())),
					null,
					null,
					Collections.emptyList(),
					null,
					new RestAPIParams.Message.AllowedMentions(
							Collections.emptyList(),
							Collections.emptyList(),
							Collections.emptyList(),
							false
					)
				);
				new Logger("FakeStickers").debug(message.toString());
				Utils.threadPool.execute(() -> {
					//Subscriptions in Java, because you can't do msg.subscribe() like in Kotlin
					RxUtils.subscribe(
							RestAPI.getApi().sendMessage(StoreStream.getChannelsSelected().getId(), message),
							RxUtils.createActionSubscriber(zz -> {})
					);
				});

				// Skip original method
				param.setResult(null);

				// Dismiss sticker picker
				var stickerListener = (WidgetChatInputAttachments$createAndConfigureExpressionFragment$stickerPickerListener$1) // What a classname jeez
						ReflectUtils.getField(param.thisObject, "stickerPickerListener");
				WidgetChatInputAttachments.access$getFlexInputFragment$p(stickerListener.this$0).r.hideExpressionTray();
			} catch (Throwable ignored) {
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
