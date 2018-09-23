package br.ufpe.cin.if710.rss

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceFragment

class RssFeedPrefActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rss_feed_pref)
    }

    // Fragmento que mostra a preference com username
    class RssFeedPreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Carrega preferences a partir de um XML
            addPreferencesFromResource(R.xml.preferencias)
        }
    }
}
