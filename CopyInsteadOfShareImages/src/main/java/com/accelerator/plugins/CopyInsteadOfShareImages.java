package com.accelerator.plugins;

import android.content.Context;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;

//Needed for settings page
import com.discord.views.CheckedSetting;
import com.aliucord.api.SettingsAPI;
import com.aliucord.widgets.BottomSheet;
import android.os.Bundle;

//import com.aliucord.wrappers.embeds.MessageEmbedWrapper;
//import com.discord.widgets.chat.list.entries.ChatListEntry;
//import com.discord.widgets.chat.list.entries.MessageEntry;
import com.aliucord.Utils;
//import com.aliucord.utils.ReflectUtils;
import com.discord.widgets.media.WidgetMedia;
import com.discord.utilities.intent.IntentUtils;
import android.widget.Toast;

import com.discord.app.AppFragment;
import android.view.View;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
//import androidx.appcompat.view.menu.MenuBuilder;
//import com.discord.R;
import com.lytefast.flexinput.R;
import androidx.core.content.ContextCompat;
import android.graphics.drawable.*;

//For setToTint
import com.discord.stores.StoreStream;
import com.discord.utilities.color.ColorCompat;
import android.graphics.Color;
//import com.aliucord.Logger;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class CopyInsteadOfShareImages extends Plugin {

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
    
    public CopyInsteadOfShareImages() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    /**
     * Tints a {@link Drawable} to {@link Color#BLACK} if a user has set light theme.
     * @param drawable Drawable
     * @return Drawable for chaining
     */
    public static Drawable tintToTheme(Context context, Drawable drawable) {
        if (drawable != null && StoreStream.getUserSettingsSystem().getTheme().equals("light")) drawable.setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal));
        return drawable;
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
		
		//Grab icon
		Drawable icon = ContextCompat.getDrawable(context,R.e.ic_link_white_24dp).mutate();
		
		final int shareButtonId = Utils.getResId("menu_media_share","id");

		// add the patch
		patcher.patch(WidgetMedia.class, methodName, methodArguments, new Hook(callFrame -> {
			var binding = WidgetMedia.access$getBinding$p((WidgetMedia) callFrame.thisObject);
			var root = binding.getRoot();
			var shareButton = root.findViewById(shareButtonId);

			//The buttons are located in WidgetMedia$onViewBoundOrOnResume$1
			// public final class b implements Toolbar.OnMenuItemClickListener
			// package b.a.d;
			
			try {
				//Old (Doesn't work for embeds and stuff)
				//var imageUri = ReflectUtils.getField(callFrame.thisObject,"imageUri").toString();
				
				//Better one
				String imageUri = ((AppFragment)callFrame.thisObject).getMostRecentIntent().getStringExtra("INTENT_MEDIA_URL");
				if (settings.getBool("replaceMediaWithCDN", true))
					imageUri = imageUri.replace("media.discordapp.net/attachments","cdn.discordapp.com/attachments");
				
				//Because I can't make a string final after it's already been created...
				final String imageUriFinal = new String(imageUri);
				
				
				if (shareButton != null)
				{
				
					shareButton.setOnClickListener( new View.OnClickListener() {
						@Override
						public void onClick (View v) {
							Utils.setClipboard(null,imageUriFinal);
							Toast.makeText(context, "Copied "+imageUriFinal, Toast.LENGTH_SHORT).show();
						}
					});

					/*
					m.checkNotNullExpressionValue(context, "context");
					   String uri2 = this.$sourceUri.toString();
					m.checkNotNullExpressionValue(uri2, "sourceUri.toString()");
					IntentUtils.performChooserSendIntent$default(context, uri2, null, 4, null);
					 */
					shareButton.setOnLongClickListener((View.OnLongClickListener) v -> {
						Toolbar toolbar = root.findViewById(Utils.getResId("action_bar_toolbar","id"));
						IntentUtils.performChooserSendIntent$default(toolbar.getContext(),imageUriFinal,null,4,null);
						return true;
					});
					
					
					//Have to tint it every time in case they change themes
					//((ActionMenuItemView)shareButton).setIcon(Utils.tintToTheme(icon));
					((ActionMenuItemView)shareButton).setIcon(tintToTheme(shareButton.getContext(),icon));
				}
				else
				{
					//Most likely there is no share button because the screen width is too small.
					//Instead append a new command to the three dot menu.
					//TODO...
					Toolbar toolbar = root.findViewById(Utils.getResId("action_bar_toolbar","id"));
					//toolbar.showOverflowMenu();
					
					//Abandon all hope, ye who enter here!
					Menu menu = toolbar.getMenu();
					MenuItem newItem = menu.add("Copy URL");
					newItem.setOnMenuItemClickListener(item -> {
						Utils.setClipboard(null,imageUriFinal);
						Toast.makeText(context, "Copied "+imageUriFinal, Toast.LENGTH_SHORT).show();
						return true;
					});
				}
				
			} catch (Exception e) {
  				  e.printStackTrace();
				Toast.makeText(context, "Oops, can't get image URL (Or something went wrong)", Toast.LENGTH_SHORT).show();
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