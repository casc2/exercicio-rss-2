package br.ufpe.cin.if710.rss

import android.app.IntentService
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
class RSSPullService : IntentService("RSSPullService") {

    override fun onHandleIntent(intent: Intent) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        Log.d("DEBUG", "Buscando Feed...")
        // Baixando o feed RSS a partir do endereço URL configurada pelo usuário se existente.
        // Do contrário, carrega a partir do endereço padrão definido em strings.xml.
        val feedXML = getRssFeed(sharedPref.getString("rssfeed", getString(R.string.rssfeed)))
        Log.d("DEBUG", "Feed: $feedXML")

        Log.d("DEBUG", "Fazendo o Parser")
        val parsedFeedXML = ParserRSS.parse(feedXML)
        persistItemsOnDatabase(parsedFeedXML)

        Log.d("DEBUG", "Enviando broadcast...")
        sendBroadcast(Intent(ACTION_UPDATE_RSS_FEED))
    }

    private fun persistItemsOnDatabase(parsedFeedXML : List<ItemRSS>) {
        Log.d("DEBUG", "Começando a persitir os itens...")
        for (itemRss in parsedFeedXML) {
            if (database.getItemRSS(itemRss.link) == null) {
                Log.d("DEBUG", "Adicionando item")
                database.insertItem(itemRss)
            }
        }
    }

    // Método auxiliar para abrir a conexão HTTP e obter o xml do feed.
    @Throws(IOException::class)
    private fun getRssFeed(feed: String): String {
        var `in`: InputStream? = null
        val rssFeed: String
        try {
            val url = URL(feed)
            val conn = url.openConnection() as HttpURLConnection
            `in` = conn.inputStream
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var count = `in`!!.read(buffer)
            while (count != -1) {
                out.write(buffer, 0, count)
                count = `in`.read(buffer)
            }
            val response = out.toByteArray()
            rssFeed = String(response, charset("UTF-8"))
        } finally {
            `in`?.close()
        }
        return rssFeed
    }

    companion object {
        const val ACTION_UPDATE_RSS_FEED = "br.ufpe.cin.if710.rss.action.UPDATE_RSS_FEED"
    }
}
