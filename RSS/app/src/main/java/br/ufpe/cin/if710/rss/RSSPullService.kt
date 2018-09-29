package br.ufpe.cin.if710.rss

import android.app.IntentService
import android.content.Intent
import android.preference.PreferenceManager
import org.xmlpull.v1.XmlPullParserException
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

    var hasNews = false

    override fun onHandleIntent(intent: Intent) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        // Baixando o feed RSS a partir do endereço URL configurada pelo usuário se existente.
        // Do contrário, carrega a partir do endereço padrão definido em strings.xml.
        try {
            val feedXML = getRssFeed(sharedPref.getString("rssfeed", getString(R.string.rssfeed)))
            val parsedFeedXML = ParserRSS.parse(feedXML)
            persistItemsOnDatabase(parsedFeedXML)
        } catch (e: XmlPullParserException){
            e.printStackTrace()
        } catch (e: IOException){
            e.printStackTrace()
        }

        sendBroadcast(Intent(ACTION_UPDATE_RSS_FEED))

        if (hasNews) {
            sendBroadcast(Intent(ACTION_SEND_NOTIFICATION))
        }
    }

    private fun persistItemsOnDatabase(parsedFeedXML : List<ItemRSS>) {
        for (itemRss in parsedFeedXML) {
            if (database.getItemRSS(itemRss.link) == null) {
                database.insertItem(itemRss)
                hasNews = true
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
        const val ACTION_UPDATE_RSS_FEED = "br.ufpe.cin.if710.rss.UPDATE_RSS_FEED"
        const val ACTION_SEND_NOTIFICATION = "br.ufpe.cin.if710.rss.ACTION_SEND_NOTIFICATION"
    }
}
