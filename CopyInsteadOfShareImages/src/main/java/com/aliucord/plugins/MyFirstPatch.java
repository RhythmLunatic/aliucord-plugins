package com.aliucord.plugins;

import android.content.Context;

import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
//import com.aliucord.wrappers.embeds.MessageEmbedWrapper;
//import com.discord.widgets.chat.list.entries.ChatListEntry;
//import com.discord.widgets.chat.list.entries.MessageEntry;
import com.aliucord.Logger;
import com.aliucord.Utils;
//import com.aliucord.utils.ReflectUtils;
import com.discord.widgets.media.WidgetMedia;
import android.widget.Toast;
import android.content.Intent;
import com.discord.app.AppFragment;
import android.view.View;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class MyFirstPatch extends Plugin {
	@Override
	// Called when your plugin is started. This is the place to register command, add patches, etc
	public void start(Context context) {
		// The full name of the class to patch
		//var className = "com.discord.widgets.media.WidgetMedia";
		// The method of that class to patch
		var methodName = "onViewBoundOrOnResume";
		// These are the arguments the patched methods receives. In the case of
		// WidgetChatListAdapterItemMessage.onConfigure the method's implementation is
		// public void onConfigure(int i, ChatListEntry chatListEntry), so our methodArguments
		// look like this:
		var methodArguments = new Class<?>[] { };
		
		final int shareButtonId = Utils.getResId("menu_media_share","id");

		// add the patch
		patcher.patch(WidgetMedia.class, methodName, methodArguments, new PinePatchFn(callFrame -> {
			var binding = WidgetMedia.access$getBinding$p((WidgetMedia) callFrame.thisObject);
			var root = binding.getRoot();
			var shareButton = root.findViewById(shareButtonId);
			try {
				
				//Old (Doesn't work for embeds and stuff)
				//var imageUri = ReflectUtils.getField(callFrame.thisObject,"imageUri").toString();
				
				
				//v2
				//var a = (Intent)ReflectUtils.invokeMethod(AppFragment.class,callFrame.thisObject,"getMostRecentIntent");
				//var imageUri = a.getStringExtra("INTENT_MEDIA_URL");
				
				//v3
				var imageUri = ((AppFragment)callFrame.thisObject).getMostRecentIntent().getStringExtra("INTENT_MEDIA_URL");
				
				shareButton.setOnClickListener( new View.OnClickListener() {
					@Override
					public void onClick (View v) {
						Utils.setClipboard(null,imageUri);
						Toast.makeText(context, "Copied "+imageUri, Toast.LENGTH_SHORT).show();
					}
				});
			} catch (Exception e) {
  				  e.printStackTrace();
				Toast.makeText(context, "Oops, can't get image URL", Toast.LENGTH_SHORT).show();
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
