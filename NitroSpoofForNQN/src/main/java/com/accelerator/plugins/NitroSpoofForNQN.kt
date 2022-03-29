package com.accelerator.plugins

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.InsteadHook
import com.discord.models.domain.emoji.ModelEmojiCustom
import de.robv.android.xposed.XC_MethodHook
import java.lang.reflect.Field

@SuppressWarnings("unused")
@AliucordPlugin
class NitroSpoofForNQN : Plugin() {

    private val reflectionCache = HashMap<String, Field>()

    override fun start(context: Context) {
        patcher.patch(
            ModelEmojiCustom::class.java.getDeclaredMethod("getChatInputText"),
            Hook { getChatReplacement(it) }
        )
        patcher.patch(
            ModelEmojiCustom::class.java.getDeclaredMethod("getMessageContentReplacement"),
            Hook { getChatReplacement(it) }
        )
        patcher.patch(
            ModelEmojiCustom::class.java.getDeclaredMethod("isUsable"),
            InsteadHook { true }
        )
        patcher.patch(
            ModelEmojiCustom::class.java.getDeclaredMethod("isAvailable"),
            InsteadHook { true }
        )
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun getChatReplacement(callFrame: XC_MethodHook.MethodHookParam) {
        val thisObject = callFrame.thisObject as ModelEmojiCustom
        val isUsable = thisObject.getCachedField<Boolean>("isUsable")

        if (isUsable) {
            callFrame.result = callFrame.result
            return
        }

        var finalUrl = "<:"+thisObject.getCachedField<String>("name")+":"
		finalUrl+=thisObject.getCachedField<String>("idStr")+">"
        //val isAnimated = thisObject.getCachedField<Boolean>("isAnimated")

        
        
        callFrame.result = finalUrl
    }

    /**
     * Get a reflected field from cache or compute it if cache is absent
     * @param V type of the field value
     */
    private inline fun <reified V> Any.getCachedField(
        name: String,
        instance: Any? = this,
    ): V {
        val clazz = this::class.java
        return reflectionCache.computeIfAbsent(clazz.name + name) {
            clazz.getDeclaredField(name).also {
                it.isAccessible = true
            }
        }.get(instance) as V
    }

}
