package com.accelerator.plugins;

import android.content.Context;

import com.aliucord.annotations.AliucordPlugin;
//import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.InsteadHook;
import com.aliucord.Logger;

import com.lytefast.flexinput.fragment.*;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class AttachmentKeyboardFix extends Plugin {

	@Override
	// Called when your plugin is started. This is the place to register command, add patches, etc
	public void start(Context context) {
		patcher.patch(FlexInputFragment$b.class,"run",new Class<?>[]{}, new InsteadHook(callFrame->{
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
