package com.aliucord.plugins;

import android.content.Context;

import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
//import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.Logger;
import com.aliucord.Utils;
//import com.aliucord.utils.ReflectUtils;
//import com.discord.media_picker.MediaPicker;
//import com.discord.media_picker.RequestType;
//import top.canyie.pine.callback.MethodReplacement;
//import androidx.core.content.FileProvider;
import android.widget.Toast;
import android.provider.MediaStore;
import android.content.*;
//import android.net;
//import android.content.pm.PackageManager;
import androidx.fragment.app.Fragment;
import com.lytefast.flexinput.fragment.FlexInputFragment;
import com.lytefast.flexinput.viewmodel.FlexInputViewModel;
//import com.discord.widgets.chat.input.*;
//import com.discord.databinding.WidgetChatInputBinding;
//import com.lytefast.flexinput.viewmodel.*;
//import com.lytefast.flexinput.fragment.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewbinding.ViewBinding;
import android.widget.ImageView;
import java.lang.reflect.Field;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class MediaPickerPatcher extends Plugin {

	/*class PluginSettings {
		@override
		onViewBound(View view) {
			var context = requireContext()
		}
	}*/
	/*public MediaPickerPatcher() {
		settingsTab = new SettingsTab(MySettingsPage.class).withArgs(settings);
	}*/
	
	
	public class DumbDecompiledCode implements View.OnLongClickListener {
		public final /* synthetic */ int i;
		public final /* synthetic */ c.b.a.a.a parentObj;

		public DumbDecompiledCode(int i2, c.b.a.a.a obj) {
			this.i = i2;
			parentObj = obj;
		}

		@Override
		public boolean onLongClick(View view) {
			if (i == 0) {
				//int i3 = c.b.a.a.a.i;
				if (parentObj.isCancelable()) {
					parentObj.h(true);
				}
			} else if (i == 1) {
				//Objects.requireNonNull(parentObj);
				Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
				intent.setType("*/*");
				intent.addCategory("android.intent.category.OPENABLE");
				intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
				try {
					parentObj.startActivityForResult(intent, 5968);
				} catch (ActivityNotFoundException unused) {
					Toast.makeText(parentObj.getContext(), "Some failure message here", 0).show();
				}
				return true;
			} else if (i == 2) {
				FlexInputFragment flexInputFragment = (FlexInputFragment) ((Fragment) parentObj);
				FlexInputViewModel flexInputViewModel = flexInputFragment.r;
				if (flexInputViewModel != null) {
					flexInputViewModel.onSendButtonClicked(flexInputFragment.n);
				}
			} else {
				throw null;
			}
			return false;
		}
	}


	@Override
	// Called when your plugin is started. This is the place to register command, add patches, etc
	public void start(Context context) {
		Logger logger = new Logger("MediaPickerPatcher");

		patcher.patch(c.b.a.a.a.class, "onCreateView", new Class<?>[]{LayoutInflater.class,ViewGroup.class,android.os.Bundle.class}, new PinePatchFn(callFrame -> {
			

			try {
				var pickerObj = (c.b.a.a.a)callFrame.thisObject;
				var pickerButton = (ImageView)pickerObj.m;
				
				//pickerButton.setVisibility(View.GONE);
				

				//Surely there is some way to do this with reflection?
				pickerButton.setOnLongClickListener(new DumbDecompiledCode(1,pickerObj));
				/*pickerButton.setOnItemLongClickListener( new View.OnLongClickListener() {
					@Override
					public void onLongClick(View v) {
						
					}
				});*/
				
				pickerButton.setOnClickListener( new View.OnClickListener() {
					@Override
					public void onClick (View v) {
						Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
						if (false)
							intent.setType("*/*");
						else
						{
							intent.setType("image/* video/*");
							intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
						}
						//intent.addCategory("android.intent.category.OPENABLE");
						//intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
						try {
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
	}

	@Override
	// Called when your plugin is stopped
	public void stop(Context context) {
		// Remove all patches
		patcher.unpatchAll();
	}
}
