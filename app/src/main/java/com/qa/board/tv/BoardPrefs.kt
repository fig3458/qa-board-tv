package com.qa.board.tv

import android.content.Context

/** Local-only board URL storage. Nothing is shipped with a default company URL. */
object BoardPrefs {
    private const val PREF = "board_prefs"
    private const val KEY_URL = "board_url"

    fun getUrl(context: Context): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_URL, "")
            ?.trim()
            .orEmpty()
    }

    fun setUrl(context: Context, url: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_URL, url.trim())
            .apply()
    }

    fun hasUrl(context: Context): Boolean = getUrl(context).isNotEmpty()

    /** Normalize user input into an http(s) URL, or empty if unusable. */
    fun normalize(raw: String): String {
        val t = raw.trim()
        if (t.isEmpty()) return ""
        val withScheme = when {
            t.startsWith("http://", ignoreCase = true) -> t
            t.startsWith("https://", ignoreCase = true) -> t
            else -> "http://$t"
        }
        return withScheme
    }
}
