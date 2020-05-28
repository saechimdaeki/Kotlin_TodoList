package Junseong.todo.myapplication

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DB(context: Context) : SQLiteOpenHelper(context,"tododb",null,2) {
    override fun onCreate(p0: SQLiteDatabase?) {
        //TODO("Not yet implemented")
        val memosql="create table tb_todo" +
                "(_id integer primary key autoincrement," +
                "title," +
                "content," +
                "date," +
                "completed)"
        p0?.execSQL(memosql);
    }
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        //TODO("Not yet implemented")
        p0?.execSQL("drop table tb_todo")
        onCreate(p0)
    }

}