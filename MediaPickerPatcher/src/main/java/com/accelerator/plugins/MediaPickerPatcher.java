package com.accelerator.plugins;

import android.content.Context;

import com.aliucord.PluginManager;
import com.aliucord.annotations.AliucordPlugin;
//import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.InsteadHook;
import com.aliucord.Logger;
import com.aliucord.Utils;
import android.widget.Toast;
import android.provider.MediaStore;
import android.content.*;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

//Needed for settings page
import com.discord.views.CheckedSetting;
import com.aliucord.api.SettingsAPI;
import com.aliucord.widgets.BottomSheet;
import android.os.Bundle;

//
import com.lytefast.flexinput.fragment.*;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class MediaPickerPatcher extends Plugin {

	public static class PluginSettings extends BottomSheet {
        private final SettingsAPI settings;

        public PluginSettings(SettingsAPI settings) { this.settings = settings; }

        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);

			//
            addView(createCheckedSetting(view.getContext(), "Allow all file types", "MMP_AllowAllFiles", false));
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
    
    public MediaPickerPatcher() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
        if (PluginManager.plugins.get("AttachmentKeyboardFix") != null && PluginManager.isPluginEnabled("AttachmentKeyboardFix"))
        {
        	Utils.showToast("Do not enable AttachmentKeyboardFix and MediaPickerPatcher at the same time, they do the same thing!!",true);
        }
    }

	@Override
	// Called when your plugin is started. This is the place to register command, add patches, etc
	public void start(Context context) {
		Logger logger = new Logger("MediaPickerPatcher");

		patcher.patch(b.b.a.a.a.class, "onCreateView", new Class<?>[]{LayoutInflater.class,ViewGroup.class,android.os.Bundle.class}, new Hook(callFrame -> {
			

			try {
				var pickerObj = (b.b.a.a.a)callFrame.thisObject;
				var pickerButton = (ImageView)pickerObj.n;
				
				//pickerButton.setVisibility(View.GONE);
				
				pickerButton.setOnLongClickListener(view -> {
					new b.b.a.a.a$a(1,pickerObj).onClick(view);
					return true;
				});
				
				pickerButton.setOnClickListener( new View.OnClickListener() {
					@Override
					public void onClick (View v) {
						Intent intent = null;

						if (settings.getBool("MMP_AllowAllFiles", false)) {
							intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.setType("*/*");
							intent = Intent.createChooser(intent, "Choose a file");
						}else
						{
							intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
							intent.setType("image/* video/*");
							intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
						}
						
						try {
							intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
							pickerObj.startActivityForResult(intent, 5968);
						} catch (ActivityNotFoundException unused) {
							Toast.makeText(pickerObj.getContext(), "lmao", 0).show();
						}
					}
				});
				logger.debug("Patched media picker button.");
				
			} catch (Exception e) {
				e.printStackTrace();
				//Toast.makeText(context, "Oops, can't get image URL", Toast.LENGTH_SHORT).show();
			}
			
			
			
		}));


		patcher.patch(FlexInputFragment$b.class,"run",new Class<?>[]{}, new InsteadHook(callFrame->{
			//logger.debug("b.run() fired");
			//logger.debug("State: "+String.valueOf(fragment.getShowsDialog()));
			
			//See "notes.txt" for more information.
			b.b.a.a.a fragment = (b.b.a.a.a)((FlexInputFragment$b)callFrame.thisObject).j;
			
			if (fragment != null && fragment.isAdded() && !fragment.isRemoving() && !fragment.isDetached()) {
				try {
					fragment.h(true); //obfuscated .dismiss() function
				} catch (IllegalStateException e) {
					logger.warn("could not dismiss add content dialog");
				}
			}
			return null;
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
