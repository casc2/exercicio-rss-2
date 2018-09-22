package br.ufpe.cin.if710.rss

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.itemlista.view.*
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import android.widget.Toast

// Adapter utilizado para popular o RecyclerView.
class ItemRssAdapter(private val rssFeed: List<ItemRSS>) :
        RecyclerView.Adapter<ItemRssAdapter.MyViewHolder>() {

    // Definindo o holder: objeto encapsula cada item da lista a ser exibido pelo RecyclerView.
    class MyViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var data: TextView? = null
        var titulo: TextView? = null

        init {
            this.data = item.item_data
            this.titulo = item.item_titulo
        }
    }

    // Evento de criação do ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ItemRssAdapter.MyViewHolder {
        val item = LayoutInflater.from(parent.context)
                .inflate(R.layout.itemlista, parent, false)
        return MyViewHolder(item)
    }

    // Evento de enlace entre as propriedades do ViewHolder e o objeto da lista de feed.
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val feedItem = rssFeed[position]

        // Bind ocorre aqui:
        holder.data?.text = feedItem.pubDate
        holder.titulo?.text = feedItem.title

        // Ao clicar em um item da lista (título ou data), um intent implícito de acesso ao navegador é configurado.
        // Antes de iniciar a Activity, verifica primeiro se há algum app instalado capaz de satisfazer o Intent.
        // Caso contrário, aborta e exibe um toast informando o erro.
        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(feedItem.link))
            intent.addCategory(CATEGORY_DEFAULT)
            intent.addCategory(CATEGORY_BROWSABLE)
            if (intent.resolveActivity(holder.itemView.context.packageManager) != null) {
                startActivity(holder.itemView.context, intent, null)
            } else {
                Toast.makeText(holder.itemView.context,
                        "Não foi possível abrir o link: navegador compatível não encontrado",
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount() = rssFeed.size
}