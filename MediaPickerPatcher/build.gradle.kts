version = "1.0.91"
description = "Replaces the document picker with one that opens the default gallery app. Can also long press the open file button for the original document picker."

aliucord {
    changelog.set(
        """
            # 1.0.91
            * Aliucord removed Pine hooks finally so I had to fix it
            # 1.0.9
            * Allow picking more than one file if your gallery app supports it, usually by holding down an image instead of tapping.
            # 1.0.8
            * Fix for Discord 112.4 (Will break on earlier versions)
            # 1.0.7
            * Fix for Discord 105.12 (Will break on earlier versions)
            # 1.0.6
            * Fix the "Allow All" setting being on by default, finally
        """.trimIndent()
    )
}
