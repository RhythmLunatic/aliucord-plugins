package com.aliucord.plugins;

import android.content.Context;

import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;

import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.utils.ReflectUtils;

import com.discord.utilities.mg_recycler.*;
import com.discord.widgets.chat.input.sticker.StickerViewHolder;
import com.discord.databinding.StickerPickerStickerItemBinding;
import com.discord.api.sticker.BaseSticker;
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
import com.aliucord.utils.RxUtils;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class FakeStickers extends Plugin {
    
    public FakeStickers() {}

	@Override
	// Called when your plugin is started. This is the place to register command, add patches, etc
	public void start(Context context) {
	
	
		// add the patch
		
		/*patcher.patch("WidgetChatInputAttachments$createAndConfigureExpressionFragment$emojiPickerListener$1",
			"WidgetChatInputAttachments$createAndConfigureExpressionFragment$emojiPickerListener$1",
			new Class<?>[] { WidgetChatInputAttachments.class }, new PinePatchFn( callFrame -> {
			
		}))*/
		
		patcher.patch(StickerViewHolder.class, "configureSticker", new Class<?>[] {MGRecyclerDataPayload.class }, new PinePatchFn(callFrame -> {
			
			try {
				//Need reflection since binding is private
				StickerPickerStickerItemBinding binding = (StickerPickerStickerItemBinding)ReflectUtils.getField(callFrame.thisObject,"binding");
				BaseSticker baseSticker = (BaseSticker)binding.b.j;
				
				binding.b.setOnClickListener(view -> {
					//Utils.showToast("https://media.discordapp.net/stickers/"+baseSticker.d()+baseSticker.b()+"?size=160",false);
					long id = StoreStream.getChannelsSelected().getId();
					if (id < 1)
					{
						Utils.showToast("You are not in a valid channel.");
					}
					else
					{
						RestAPIParams.Message message = new RestAPIParams.Message(
							"https://cdn.discordapp.com/stickers/"+baseSticker.d()+baseSticker.b(),
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
						new Logger("FakeStickers").debug(Long.toString(id));
						new Logger("FakeStickers").debug(message.toString());
						Utils.threadPool.execute(() -> {
							RxUtils.subscribe(RestAPI.getApi().sendMessage(id, message),RxUtils.createActionSubscriber(zz -> {}));
						});
						
						// try {
						// 	JSONObject json = new JSONObject()
						// 	.put("content","https://media.discordapp.net/stickers/"+baseSticker.d()+baseSticker.b()+"?size=160")
						// 	.put("nonce",Long.toString(NonceGenerator.computeNonce(ClockFactory.get())))
						// 	.put("tts",false);
							
						// 	Http.Request req = new Http.Request("https://discord.com/api/v9/channels/"+Long.toString(id)+"/messages", "POST")
						// 	.setHeader("Authorization", (String)ReflectUtils.getField(StoreStream.getAuthentication(), "authToken"))
						// 	.setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.getUserAgent())
						// 	.setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.getSuperPropertiesStringBase64())
						// 	.setHeader("Accept", "*/*");
							
						// 	req.executeWithJson(json.toString());
						// }
						// catch (Exception e) {
			  			// 	e.printStackTrace();
						// 	Utils.showToast("Oops", false);
						// }
					}
					
				});
				
				//new Logger("FakeStickers").debug("https://media.discordapp.net/stickers/"+baseSticker.d()+baseSticker.b()+"?size=160");
				
				/*String str = baseSticker.d() + baseSticker.b();
				File file2 = new File(file, str);
				if (!file2.exists()) {
					return DownloadUtils.downloadFile(context, getCDNAssetUrl$default(this, baseSticker, null, false, 6, null), str, file);
				}*/
				
			} catch (Exception e) {
  				e.printStackTrace();
				Utils.showToast("Oops", false);
			}
			
			//shareButton.setVisibility(View.GONE);
			
		}));
	}

	@Override
	// Called when your plugin is stopped
	public void stop(Context context) {
		// Remove all patches
		patcher.unpatchAll();
	}
}
