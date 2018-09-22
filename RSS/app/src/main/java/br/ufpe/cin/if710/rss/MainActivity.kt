package br.ufpe.cin.if710.rss

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

// Classe MainActivity portada para Kotlin.
class MainActivity : Activity() {
    // Declaração de variáveis necessárias para inicializar o ReciyclerView.
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emptyAdapter: List<ItemRSS> = emptyList()

        // Optar por exibição com linear layout, ao invés de grid layout.
        viewManager = LinearLayoutManager(this)

        // O adapter preenchido com as notícias a serem exibidas só será criado após se obter o response da URL.
        // Para evitar erro de adapter inexistente, ele é inicializado com uma lista vazia do tipo ItemRSS.
        viewAdapter = ItemRssAdapter(emptyAdapter)

        // Inicializando o RecyclerView com os parâmetros definidos a cima, utilizando o mesmo id do antigo TextView.
        recyclerView = findViewById<RecyclerView>(R.id.conteudoRSS).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        // Busca o feed RSS da URL definida em res/values/strings.xml.
        loadRSS(getString(R.string.rssfeed))
    }

    private fun loadRSS(rssFeed: String) {
        // Faz o carregamento do XML de maneira assíncrona e fora da main thread.
        doAsync {
            val feedXML = getRssFeed(rssFeed)

            uiThread {
                // Realiza o parsing do XML e cria o adapter no qual cada matéria é uma objeto do tipo ItemRSS.
                val parsedFeedXML = ParserRSS.parse(feedXML)
                viewAdapter = ItemRssAdapter(parsedFeedXML)

                // Atualiza o RecyclerView com o novo adapter na main thread.
                recyclerView.apply {
                    adapter = viewAdapter
                }
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
