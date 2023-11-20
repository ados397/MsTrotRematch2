package com.ados.mstrotrematch2.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelperCheeringBoard(context: Context)
    : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "RematchCheeringBoard"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "t_cheering_board"
        private const val COL_POST_ID = "PostID"
        private const val COL_IS_LIKE = "IsLike"
        private const val COL_IS_DISLIKE = "IsDislike"
        private const val COL_IS_BLOCK = "IsBlock"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable =
            "CREATE TABLE $TABLE_NAME" +
                    "($COL_POST_ID TEXT PRIMARY KEY," +
                    "$COL_IS_LIKE INTEGER DEFAULT 0," +
                    "$COL_IS_DISLIKE INTEGER DEFAULT 0," +
                    "$COL_IS_BLOCK INTEGER DEFAULT 0)"
        db?.execSQL(createTable)
        println("아이템 $createTable")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    private fun isRow(docName: String) : Boolean {
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME WHERE $COL_POST_ID = '$docName'"
        val cursor = db.rawQuery(selectALLQuery, null)
        var isRow = false
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isRow = true
            }
        }
        cursor.close()
        db.close()

        return isRow
    }

    fun updateLike(docName: String, like: Int): Boolean {
        return if (isRow(docName)) { // 값이 있으면 update
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(COL_POST_ID, docName)
            values.put(COL_IS_LIKE, like)
            val success = db.update(TABLE_NAME, values, "$COL_POST_ID=?", arrayOf(docName))
            db.close()
            (Integer.parseInt("$success") != -1)
        } else { // 없으면 insert
            insertData(docName, like, 0, 0)
        }
    }

    fun updateDislike(docName: String, dislike: Int): Boolean {
        return if (isRow(docName)) { // 값이 있으면 update
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(COL_POST_ID, docName)
            values.put(COL_IS_DISLIKE, dislike)
            val success = db.update(TABLE_NAME, values, "$COL_POST_ID=?", arrayOf(docName))
            db.close()
            (Integer.parseInt("$success") != -1)
        } else { // 없으면 insert
            insertData(docName, 0, dislike, 0)
        }
    }

    fun updateBlock(name: String, block: Int): Boolean {
        return if (isRow(name)) { // 값이 있으면 update
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(COL_POST_ID, name)
            values.put(COL_IS_BLOCK, block)
            val success = db.update(TABLE_NAME, values, "$COL_POST_ID=?", arrayOf(name))
            db.close()
            (Integer.parseInt("$success") != -1)
        } else { // 없으면 insert
            insertData(name, 0, 0, block)
        }
    }

    private fun insertData(docName: String, like: Int, dislike: Int, block: Int) : Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_POST_ID, docName)
        values.put(COL_IS_LIKE, like)
        values.put(COL_IS_DISLIKE, dislike)
        values.put(COL_IS_BLOCK, block)

        var success = db.insert(TABLE_NAME, null, values)
        db.close()

        return (Integer.parseInt("$success") != -1)
    }


    /*fun updateLike(item: BoardDTO, like: Int) {
        var query = "INSERT OR REPLACE INTO $TABLE_NAME ($POST_ID, $IS_LIKE) VALUES ('${item.docname}', $like)"
        val db = this.writableDatabase
        db?.execSQL(query)
        db.close()
    }

    fun updateDislike(item: BoardDTO, dislike: Int) {
        var query = "INSERT OR REPLACE INTO $TABLE_NAME ($POST_ID, $IS_DISLIKE) VALUES ('${item.docname}', $dislike)"
        val db = this.writableDatabase
        db?.execSQL(query)
        db.close()
    }*/

    fun getLike(docName: String) : Boolean {
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME WHERE $COL_POST_ID = '$docName'"
        println("아이템 $selectALLQuery")
        val cursor = db.rawQuery(selectALLQuery, null)
        var isLike = 0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val value = cursor.getColumnIndex(COL_IS_LIKE)
                isLike = cursor.getInt(value)
            }
        }
        cursor.close()
        db.close()

        return isLike == 1
    }

    fun getDislike(docName: String) : Boolean {
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME WHERE $COL_POST_ID = '$docName'"
        val cursor = db.rawQuery(selectALLQuery, null)
        var isDislike = 0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val value = cursor.getColumnIndex(COL_IS_DISLIKE)
                isDislike = cursor.getInt(value)

                println("싫어요 결과 : $isDislike")
            }
        }
        cursor.close()
        db.close()

        return isDislike == 1
    }

    fun getblock(name: String) : Boolean {
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME WHERE $COL_POST_ID = '$name'"
        val cursor = db.rawQuery(selectALLQuery, null)
        var isBlock = 0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val value = cursor.getColumnIndex(COL_IS_BLOCK)
                isBlock = cursor.getInt(value)

                println("차단 결과 : $isBlock")
            }
        }
        cursor.close()
        db.close()

        return isBlock == 1
    }
}
