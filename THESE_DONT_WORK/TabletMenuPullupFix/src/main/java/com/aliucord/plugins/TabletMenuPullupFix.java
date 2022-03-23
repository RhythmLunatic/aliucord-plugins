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
import com.google.android.material.bottomsheet.BottomSheetBehavior;


// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class AttachmentKeyboardFix extends Plugin {

	@Override
	// Called when your plugin is started. This is the place to register command, add patches, etc
	public void start(Context context) {
		patcher.patch(BottomSheetBehavior.class,"onViewReleased",new Class<?>[]{View.class, float.class, float.class}, new PineInsteadFn(callFrame->{
			BottomSheetBehavior bs = (BottomSheetBehavior)callFrame;
			bs.startSettlingAnimation(
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
