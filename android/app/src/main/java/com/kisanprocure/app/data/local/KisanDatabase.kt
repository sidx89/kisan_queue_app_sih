package com.kisanprocure.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.kisanprocure.app.data.model.Booking

class KisanDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "kisan_procure_cache.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_BOOKINGS = "cached_bookings"
        private const val COL_ID = "id"
        private const val COL_TOKEN = "booking_token"
        private const val COL_STATUS = "status"
        private const val COL_CENTRE_NAME = "centre_name"
        private const val COL_SLOT_DATE = "slot_date"
        private const val COL_SLOT_TIME = "slot_time"
        private const val COL_CROP_NAME = "crop_name"
        private const val COL_QUANTITY = "estimated_quantity_kg"

        @Volatile
        private var INSTANCE: KisanDatabase? = null

        fun getInstance(context: Context): KisanDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KisanDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_BOOKINGS (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_TOKEN TEXT NOT NULL,
                $COL_STATUS TEXT NOT NULL,
                $COL_CENTRE_NAME TEXT,
                $COL_SLOT_DATE TEXT,
                $COL_SLOT_TIME TEXT,
                $COL_CROP_NAME TEXT,
                $COL_QUANTITY REAL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKINGS")
        onCreate(db)
    }

    fun saveBookings(bookings: List<Booking>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_BOOKINGS, null, null)
            for (b in bookings) {
                val values = ContentValues().apply {
                    put(COL_ID, b.id)
                    put(COL_TOKEN, b.bookingToken)
                    put(COL_STATUS, b.status)
                    put(COL_CENTRE_NAME, b.centreName)
                    put(COL_SLOT_DATE, b.slotDate)
                    put(COL_SLOT_TIME, b.slotTime)
                    put(COL_CROP_NAME, b.cropName)
                    put(COL_QUANTITY, b.estimatedQuantityKg)
                }
                db.insertWithOnConflict(TABLE_BOOKINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCachedBookings(): List<Booking> {
        val list = mutableListOf<Booking>()
        val db = readableDatabase
        val cursor = db.query(TABLE_BOOKINGS, null, null, null, null, null, "$COL_ID DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Booking(
                        id = it.getInt(it.getColumnIndexOrThrow(COL_ID)),
                        bookingToken = it.getString(it.getColumnIndexOrThrow(COL_TOKEN)),
                        status = it.getString(it.getColumnIndexOrThrow(COL_STATUS)),
                        centreName = it.getString(it.getColumnIndexOrThrow(COL_CENTRE_NAME)),
                        slotDate = it.getString(it.getColumnIndexOrThrow(COL_SLOT_DATE)),
                        slotTime = it.getString(it.getColumnIndexOrThrow(COL_SLOT_TIME)),
                        cropName = it.getString(it.getColumnIndexOrThrow(COL_CROP_NAME)),
                        estimatedQuantityKg = it.getDouble(it.getColumnIndexOrThrow(COL_QUANTITY))
                    )
                )
            }
        }
        return list
    }
}
