package net.j2i.webcrawler.data
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

const val DATABASE_NAME = "url_log"
const val DATABASE_VERSION = 3

class UrlReadingDataHelper(context:Context):SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    object UrlReadingsContract:BaseColumns {
        const val TABLE_NAME = "url_reading"
        const val COLUMN_NAME_SESSION_ID = "sessionID"
        const val COLUMN_NAME_PAGE_REQUEST_ID = "pageRequestID"
        const val COLUMN_NAME_URL = "url"
        const val COLUMN_NAME_TIMESTAMP = "timestamp"

        const val CREATE_TABLE_QUERY = "CREATE TABLE ${UrlReadingsContract.TABLE_NAME} ("+
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${UrlReadingsContract.COLUMN_NAME_SESSION_ID} INTEGER,"+
                "${UrlReadingsContract.COLUMN_NAME_PAGE_REQUEST_ID} INTEGER,"+
                "${UrlReadingsContract.COLUMN_NAME_URL} TEXT,"+
                "${UrlReadingsContract.COLUMN_NAME_TIMESTAMP} INTEGER"+
                ")"
        const val DELETE_TABLES_QUERY = "DROP TABLE IF EXISTS ${UrlReadingsContract.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(UrlReadingsContract.CREATE_TABLE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(UrlReadingsContract.DELETE_TABLES_QUERY)
        onCreate(db)
    }

    fun insertUrlReadingList(readings:List<UrlReading>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            readings.forEach { reading ->
                insertReading(reading)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun insertReading(reading:UrlReading) {

        val db = writableDatabase
        val values = ContentValues().apply {
            put(UrlReadingsContract.COLUMN_NAME_SESSION_ID, reading.sessionID)
            put(UrlReadingsContract.COLUMN_NAME_PAGE_REQUEST_ID, reading.pageRequestID)
            put(UrlReadingsContract.COLUMN_NAME_URL, reading.url)
            put(UrlReadingsContract.COLUMN_NAME_TIMESTAMP, reading.timestamp)
        }

        val newRowId = db?.insert(UrlReadingsContract.TABLE_NAME, null, values)
    }
    fun clearDatabase() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL(UrlReadingsContract.DELETE_TABLES_QUERY)
            db.execSQL(UrlReadingsContract.CREATE_TABLE_QUERY)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllReadings():List<UrlReading>  {
        val readings = mutableListOf<UrlReading>()
        val db = writableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            UrlReadingsContract.COLUMN_NAME_SESSION_ID,
            UrlReadingsContract.COLUMN_NAME_PAGE_REQUEST_ID,
            UrlReadingsContract.COLUMN_NAME_URL,
            UrlReadingsContract.COLUMN_NAME_TIMESTAMP
        )
        val sortOrder = "${UrlReadingsContract.COLUMN_NAME_TIMESTAMP} ASC"
        val cursor = db.query(
            UrlReadingsContract.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOrder
        )
        with(cursor) {
            while (moveToNext()) {
                val reading = UrlReading(
                    //source = getString(getColumnIndexOrThrow(BaseColumns._ID)),
                    sessionID = getLong(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_SESSION_ID)),
                    pageRequestID = getLong(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_PAGE_REQUEST_ID)),
                    url = getString(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_URL)),
                    timestamp = getLong(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_TIMESTAMP)),
                );
                readings.add(reading)
            }
        }
        return readings
    }

    fun getReadings(sessionID:Long):List<UrlReading> {
        val readings = mutableListOf<UrlReading>()
        val db = writableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            UrlReadingsContract.COLUMN_NAME_SESSION_ID,
            UrlReadingsContract.COLUMN_NAME_PAGE_REQUEST_ID,
            UrlReadingsContract.COLUMN_NAME_URL,
            UrlReadingsContract.COLUMN_NAME_TIMESTAMP
        )
        val selection = "${UrlReadingsContract.COLUMN_NAME_SESSION_ID} = ?"
        val selectionArgs = arrayOf(sessionID.toString())
        val sortOrder = "${UrlReadingsContract.COLUMN_NAME_TIMESTAMP} ASC"
        val cursor = db.query(
            UrlReadingsContract.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
        with(cursor) {
            while (moveToNext()) {
                val reading = UrlReading(

                    sessionID = getLong(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_SESSION_ID)),
                    pageRequestID = getLong(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_PAGE_REQUEST_ID)),
                    url = getString(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_URL)),
                    timestamp = getLong(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_TIMESTAMP))

                )
                readings.add(reading)
            }
        }
        cursor.close()
        return readings
    }

    fun writeAllRecords(os:OutputStreamWriter):List<UrlReading>  {

        os.write("SessionID, PageRequestID, Timestamp, URL\r\n")

        val readings = mutableListOf<UrlReading>()
        val db = writableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            UrlReadingsContract.COLUMN_NAME_SESSION_ID,
            UrlReadingsContract.COLUMN_NAME_PAGE_REQUEST_ID,
            UrlReadingsContract.COLUMN_NAME_URL,
            UrlReadingsContract.COLUMN_NAME_TIMESTAMP
        )
        val sortOrder = "${UrlReadingsContract.COLUMN_NAME_TIMESTAMP} ASC"
        val cursor = db.query(
            UrlReadingsContract.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOrder
        )
                    with(cursor) {
            while (moveToNext()) {
                val reading = UrlReading(
                    //source = getString(getColumnIndexOrThrow(BaseColumns._ID)),
                    sessionID = getLong(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_SESSION_ID)),
                    pageRequestID = getLong(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_PAGE_REQUEST_ID)),
                    url = getString(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_URL)),
                    timestamp = getLong(getColumnIndexOrThrow(UrlReadingsContract.COLUMN_NAME_URL)),
                );

                val line = "${reading.sessionID}; ${reading.pageRequestID}; ${reading.timestamp}, ${reading.url}\r\n";
                os.write(line);
                readings.add(reading)
            }
        }
        return readings
    }



}