package com.aliucord.plugins;

import android.content.Context;

import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
//import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
//import com.aliucord.patcher.PinePatchFn;
import com.aliucord.patcher.PineInsteadFn;
import com.aliucord.Logger;
import com.aliucord.Utils;
//import android.widget.Toast;
//import android.provider.MediaStore;
//import android.content.*;
//import android.view.LayoutInflater;
//import com.discord.widgets.chat.input.*;
//import java.util.List;

//import android.view.View;
//import android.view.ViewGroup;
//import androidx.viewbinding.ViewBinding;
//import android.widget.ImageView;
//import java.lang.reflect.Field;

//Needed for settings page
//import com.discord.views.CheckedSetting;
//import com.aliucord.api.SettingsAPI;
//import com.aliucord.widgets.BottomSheet;
//import android.os.Bundle;

//
//import java.util.ArrayList;
import com.lytefast.flexinput.fragment.*;
import com.lytefast.flexinput.model.*;
import androidx.fragment.app.DialogFragment;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class AttachmentKeyboardFix extends Plugin {

	@Override
	// Called when your plugin is started. This is the place to register command, add patches, etc
	public void start(Context context) {
		patcher.patch(FlexInputFragment$b.class,"run",new Class<?>[]{}, new PineInsteadFn(callFrame->{
			//logger.debug("b.run() fired");
			//logger.debug("State: "+String.valueOf(fragment.getShowsDialog()));
			b.b.a.a.a fragment = (b.b.a.a.a)((FlexInputFragment$b)callFrame.thisObject).j;
			
			if (fragment != null && fragment.isAdded() && !fragment.isRemoving() && !fragment.isDetached()) {
				try {
					fragment.h(true);
				} catch (IllegalStateException e) {
					new Logger("AttachmentKBFix").warn("could not dismiss add content dialog");
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
