version = "1.1.0"
description = "Posts a sticker as an image if the sticker is unavailable normally (Usually when you don't have Nitro). Lottie stickers are unsupported."

aliucord {
    changelogMedia.set("https://cdn.discordapp.com/stickers/883809297216192573.png")
    changelog.set(
        """
            # 1.1.0
            * Make sticker picker automatically close after selecting a sticker
            * Do not mark stickers as unusable (monochrome filter)
        """.trimIndent()
    )
    author("Vendicated", 343383572805058560L)
}