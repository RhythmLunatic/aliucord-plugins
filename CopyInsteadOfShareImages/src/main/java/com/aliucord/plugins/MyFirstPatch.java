package com.aliucord.plugins;

import android.content.Context;

import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;

//Needed for settings page
import com.discord.views.CheckedSetting;
import com.aliucord.api.SettingsAPI;
import com.aliucord.widgets.BottomSheet;
import android.os.Bundle;

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
//import com.discord.R;
import com.lytefast.flexinput.R;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class MyFirstPatch extends Plugin {

	public static class PluginSettings extends BottomSheet {
        private final SettingsAPI settings;

        public PluginSettings(SettingsAPI settings) { this.settings = settings; }

        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);

            addView(createCheckedSetting(view.getContext(), "Replace media.discordapp.net with cdn.discordapp.com", "replaceMediaWithCDN", true));
        }

        private CheckedSetting createCheckedSetting(Context ctx, String title, String setting, boolean checked) {
            CheckedSetting checkedSetting = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, null);

            checkedSetting.setChecked(settings.getBool(setting, checked));
            checkedSetting.setOnCheckedListener( check -> {
                settings.setBool(setting, check);
            });

            return checkedSetting;
        }
    }
    
    public MyFirstPatch() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }


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
				//shareButton.setIcon(R.d.ic_copy_24dp);
				//Old (Doesn't work for embeds and stuff)
				//var imageUri = ReflectUtils.getField(callFrame.thisObject,"imageUri").toString();
				
				
				//v2
				//var a = (Intent)ReflectUtils.invokeMethod(AppFragment.class,callFrame.thisObject,"getMostRecentIntent");
				//var imageUri = a.getStringExtra("INTENT_MEDIA_URL");
				
				//v3
				String imageUri = ((AppFragment)callFrame.thisObject).getMostRecentIntent().getStringExtra("INTENT_MEDIA_URL");
				if (settings.getBool("replaceMediaWithCDN", true))
					imageUri = imageUri.replace("media.discordapp.net","cdn.discordapp.com");
				
				//Because I can't make a string final after it's already been created...
				final String imageUriFinal = new String(imageUri);
				
				
				shareButton.setOnClickListener( new View.OnClickListener() {
					@Override
					public void onClick (View v) {
						Utils.setClipboard(null,imageUriFinal);
						Toast.makeText(context, "Copied "+imageUriFinal, Toast.LENGTH_SHORT).show();
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
