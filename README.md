# `Aliucord-plugins`

Cool Aliucord plugins. Click the plugin name to download it.

## [Copy Instead Of Share Images](https://github.com/RhythmLunatic/aliucord-plugins/raw/builds/CopyInsteadOfShareImages.zip)
Replaces the share button in the image/video preview with one that only copies the URL to the clipboard. Can also automatically replace media.discordapp with cdn.discordapp to fix videos.

## [Show All Twitter Images](https://github.com/RhythmLunatic/aliucord-plugins/raw/builds/ShowAllTwitterImages.zip)
Shows the remaining twitter images if someone posts a twitter link with more than one image.

Or in more technical terms, hooks the method that displays embeds and adds any remaining hidden embeds.

Now supports displaying twitter videos, although the name hasn't been changed :V

Special thanks to HalalKing for some regex help and [mantikafasi](https://github.com/mantikafasi/AliucordPlugins/tree/main/InvisibleMessages/src/main/java/com/aliucord/plugins) for the code I kanged to add video support.

## [FakeStickers](https://github.com/RhythmLunatic/aliucord-plugins/raw/builds/FakeStickers.zip)
Posts a sticker as an image if the sticker is unavailable normally. Does not work for lottie stickers, to see animated stickers you will need the [AnimateApngs](https://github.com/Vendicated/AliucordPlugins/blob/builds/AnimateApngs.zip) plugin.

## [MediaPickerPatcher](https://github.com/RhythmLunatic/aliucord-plugins/raw/builds/MediaPickerPatcher.zip)
Replaces the document picker with one that opens the default gallery app. Can also long press the button for original behavior.

Also allows picking videos from the gallery if your default app parses intents properly.

Includes a fix for the keyboard bug (read below).

(This is actually a bit of a misnomer, Discord has a class named MediaPicker that is used for guilds and profile pictures but it doesn't patch that)

## [AttachmentKeyboardFix](https://github.com/RhythmLunatic/aliucord-plugins/raw/builds/AttachmentKeyboardFix.zip)
Fixes a Discord bug where picking an attachment and typing something brings the attachment picker back up.

**This is only for people that don't want to install MediaPickerPatcher, as MediaPickerPatcher already comes with this fix.**

# Special Thanks
zt, Ven Halen, Not Wing, and everyone else who answered my questions when I know virtually nothing about Java/Android

# License
GPLv3, if you would like an exception contact me.

# Support
Please file an issue on the github repo if something is not working correctly.

# Donate

Like the plugins? Support me by donating. Donating means I can afford a better laptop and I don't have to write plugins in gedit.

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/D1D7Y21A)
