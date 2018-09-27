package br.ufpe.cin.if710.rss

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    private var conteudoRSS: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PreferenceManager.setDefaultValues(this, R.xml.preferencias, false)

        conteudoRSS = findViewById(R.id.conteudoRSS)

        val adapter = SimpleCursorAdapter(
                this,
                R.layout.itemlista,
                null,
                arrayOf(SQLiteRSSHelper.ITEM_TITLE, SQLiteRSSHelper.ITEM_DATE),
                intArrayOf(R.id.item_titulo, R.id.item_data),
                0
        )

        conteudoRSS?.apply {
            this.adapter = adapter
            isTextFilterEnabled = true
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val mAdapter = parent.adapter as SimpleCursorAdapter
                val mCursor = mAdapter.getItem(position) as Cursor
                val itemLink = mCursor.getString(mCursor.getColumnIndexOrThrow(SQLiteRSSHelper.ITEM_LINK))

                doAsync {
                    database.markAsRead(itemLink)
                    Log.d("DB", "Marcando \"$itemLink\" como lido.")
                }

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(itemLink))
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                if (intent.resolveActivity(context.packageManager) != null) {
                    ContextCompat.startActivity(context, intent, null)
                } else {
                    Toast.makeText(context,
                            "Não foi possível abrir o link: navegador compatível não encontrado",
                            Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_feed -> {
            // User chose the "RSS Feed" item, start activity...
            startActivity(Intent(applicationContext, RssFeedPrefActivity::class.java))
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        // Busca o feed RSS da URL definida pelo usuário ou a padrão definida em res/values/strings.xml.
        loadRSS(sharedPref.getString("rssfeed", getString(R.string.rssfeed)))
    }

    override fun onDestroy() {
        super.onDestroy()
        database.close()
    }

    private fun loadRSS(rssFeed: String) {
        // Faz o carregamento do XML de maneira assíncrona e fora da main thread.
        doAsync {
            val feedXML = getRssFeed(rssFeed)

            // Realiza o parsing do XML e cria o adapter no qual cada matéria é uma objeto do tipo ItemRSS.
            val parsedFeedXML = ParserRSS.parse(feedXML)

            for (itemRss in parsedFeedXML) {
                if (database.getItemRSS(itemRss.link) == null) {
                    database.insertItem(itemRss)
                    Log.d("DB", "Inserindo \"${itemRss.link}\" no banco.")
                }
            }

            val cursor = database.items

            Log.d("DB", cursor.count.toString())

            uiThread {
                (conteudoRSS?.adapter as CursorAdapter).changeCursor(cursor)
            }
        }
    }

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
}
