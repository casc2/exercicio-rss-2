package br.ufpe.cin.if710.rss

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import org.jetbrains.anko.doAsync
import android.content.IntentFilter
import android.util.Log
import android.os.AsyncTask



class MainActivity : Activity() {

    var conteudoRSS: ListView? = null
    private val intentFilter = IntentFilter(RSSPullService.ACTION_UPDATE_RSS_FEED)
    private val broadcastReceiver = DynamicReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("DEBUG", "Entrando no OnCreate")
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
            onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                val mAdapter = parent.adapter as SimpleCursorAdapter
                val mCursor = mAdapter.getItem(position) as Cursor
                val itemLink = mCursor.getString(mCursor.getColumnIndexOrThrow(SQLiteRSSHelper.ITEM_LINK))

                doAsync {
                    database.markAsRead(itemLink)
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
        Log.d("DEBUG", "Entrando no OnStart")

        super.onStart()

        Log.d("DEBUG", "Registrando broadcast...")
        registerReceiver(broadcastReceiver, intentFilter)

        val serviceIntent = Intent(applicationContext, RSSPullService::class.java)
        Log.d("DEBUG", "Iniciando o service...")
        startService(serviceIntent)
    }

    override fun onStop() {
        super.onStop()
        Log.d("DEBUG", "Removendo o registro...")
        unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        database.close()
    }

    class DynamicReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.d("DEBUG", "Broadcast recebido")
            val mainActivity = MainActivity()
//            mainActivity.ExibirFeed().execute()
        }
    }

    internal inner class ExibirFeed : AsyncTask<Void, Void, Cursor>() {

        override fun doInBackground(vararg voids: Void): Cursor {
            val c = database.items
            c.count
            return c
        }

        override fun onPostExecute(c: Cursor?) {
            if (c != null) {
                (conteudoRSS?.adapter as CursorAdapter).changeCursor(c)
            }
        }
    }
}
