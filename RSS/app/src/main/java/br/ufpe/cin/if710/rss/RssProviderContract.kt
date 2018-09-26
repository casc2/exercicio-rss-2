package br.ufpe.cin.if710.rss

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns

object RssProviderContract {

    const val _ID = BaseColumns._ID
    const val TITLE = "title"
    const val DATE = "pubDate"
    const val DESCRIPTION = "description"
    const val LINK = "guid"
    const val UNREAD = "unread"
    const val ITEMS_TABLE = "items"


    val ALL_COLUMNS = arrayOf(_ID, TITLE, DATE, DESCRIPTION, LINK, UNREAD)

    private val BASE_RSS_URI = Uri.parse("content://br.ufpe.cin.if710.rss/")

    val ITEMS_LIST_URI = Uri.withAppendedPath(BASE_RSS_URI, ITEMS_TABLE)

    val CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/RssProvider.data.text"

    val CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/RssProvider.data.text"

}
