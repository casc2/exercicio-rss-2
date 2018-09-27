package br.ufpe.cin.if710.rss

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteRSSHelper private constructor(c: Context) : SQLiteOpenHelper(c, DATABASE_NAME, null, DB_VERSION) {

    val items: Cursor
        @Throws(SQLException::class)
        get() {
            val db = readableDatabase
            val projection = columns
            val selection = "$ITEM_UNREAD = ?"
            val selectionArgs = arrayOf("1")

            return db.query(
                    DATABASE_TABLE,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            )
        }

    override fun onCreate(db: SQLiteDatabase) {
        //Executa o comando de criação de tabela
        db.execSQL(CREATE_DB_COMMAND)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //estamos ignorando esta possibilidade no momento
        throw RuntimeException("nao se aplica")
    }

    //IMPLEMENTAR ABAIXO
    //Implemente a manipulação de dados nos métodos auxiliares para não ficar criando consultas manualmente
    fun insertItem(item: ItemRSS): Long {
        return insertItem(item.title, item.pubDate, item.description, item.link)
    }

    private fun insertItem(title: String, pubDate: String, description: String, link: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(ITEM_TITLE, title)
            put(ITEM_DATE, pubDate)
            put(ITEM_DESC, description)
            put(ITEM_LINK, link)
            put(ITEM_UNREAD, true)
        }
        return db.insert(DATABASE_TABLE, null, values)
    }

    @Throws(SQLException::class)
    fun getItemRSS(link: String): ItemRSS? {
        var itemRSS: ItemRSS? = null
        val db = readableDatabase
        val projection = arrayOf(ITEM_TITLE, ITEM_DATE, ITEM_DESC, ITEM_LINK)
        val selection = "$ITEM_LINK = ?"
        val selectionArgs = arrayOf(link)

        val cursor : Cursor =  db.query(
            DATABASE_TABLE,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        with(cursor) {
            if(moveToNext()) {
                itemRSS = ItemRSS(
                        getString(getColumnIndexOrThrow(ITEM_TITLE)),
                        getString(getColumnIndexOrThrow(ITEM_LINK)),
                        getString(getColumnIndexOrThrow(ITEM_DATE)),
                        getString(getColumnIndexOrThrow(ITEM_DESC))
                )
            }
        }

        cursor.close()
        return itemRSS
    }

    fun markAsUnread(link: String): Boolean {
        val db = writableDatabase
        val unread = true
        val values = ContentValues().apply {
            put(ITEM_UNREAD, unread)
        }
        val selection = "$ITEM_LINK = ?"
        val selectionArgs = arrayOf(link)
        val count = db.update(
                DATABASE_TABLE,
                values,
                selection,
                selectionArgs
        )
        return (count > 0)
    }

    fun markAsRead(link: String): Boolean {
        val db = writableDatabase
        val unread = false
        val values = ContentValues().apply {
            put(ITEM_UNREAD, unread)
        }
        val selection = "$ITEM_LINK = ?"
        val selectionArgs = arrayOf(link)
        val count = db.update(
                DATABASE_TABLE,
                values,
                selection,
                selectionArgs
        )
        return (count > 0)
    }

    companion object {
        //Nome do Banco de Dados
        private const val DATABASE_NAME = "rss"
        //Nome da tabela do Banco a ser usada
        const val DATABASE_TABLE = "items"
        //Versão atual do banco
        private const val DB_VERSION = 1

        private var db: SQLiteRSSHelper? = null

        //Definindo Singleton
        @Synchronized
        fun getInstance(c: Context): SQLiteRSSHelper {
            if (db == null) {
                db = SQLiteRSSHelper(c.applicationContext)
            }
            return db!!
        }

        //Definindo constantes que representam os campos do banco de dados
        private const val ITEM_ROWID = RssProviderContract._ID
        const val ITEM_TITLE = RssProviderContract.TITLE
        const val ITEM_DATE = RssProviderContract.DATE
        const val ITEM_DESC = RssProviderContract.DESCRIPTION
        const val ITEM_LINK = RssProviderContract.LINK
        const val ITEM_UNREAD = RssProviderContract.UNREAD

        //Definindo constante que representa um array com todos os campos
        val columns = arrayOf(ITEM_ROWID, ITEM_TITLE, ITEM_DATE, ITEM_DESC, ITEM_LINK, ITEM_UNREAD)

        //Definindo constante que representa o comando de criação da tabela no banco de dados
        private const val CREATE_DB_COMMAND = "CREATE TABLE " + DATABASE_TABLE + " (" +
                    ITEM_ROWID + " integer primary key autoincrement, " +
                    ITEM_TITLE + " text not null, " +
                    ITEM_DATE + " text not null, " +
                    ITEM_DESC + " text not null, " +
                    ITEM_LINK + " text not null, " +
                    ITEM_UNREAD + " boolean not null);"
    }

}
// Access property for Context
val Context.database: SQLiteRSSHelper
    get() = SQLiteRSSHelper.getInstance(applicationContext)