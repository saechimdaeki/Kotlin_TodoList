package Junseong.todo.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_main.view.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var list: MutableList<ItemVO> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        selectDB()
        fab.setOnClickListener{
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivityForResult(intent, 10)
        }
    }
    private fun selectDB(){
        list = mutableListOf()
        val helper = DB(this)
        val db = helper.readableDatabase
        val cursor = db.rawQuery("select * from tb_todo order by date desc", null)
        var preDate: Calendar? = null
        while (cursor.moveToNext()) {
            val dbdate=cursor.getString(3)
            val date = SimpleDateFormat("yyyy-MM-dd").parse(dbdate)
            val currentDate = GregorianCalendar()
            currentDate.time = date
            if(!currentDate.equals(preDate)) {
                val headerItem = HeaderItem(dbdate)
                list.add(headerItem)
                preDate=currentDate
            }
            val completed= cursor.getInt(4) != 0
            val dataItem = DataItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    completed
            )
            list.add(dataItem)
        }
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = MyAdapter(list)
        recyclerview.addItemDecoration(MyDecoration())
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==10 && resultCode== Activity.RESULT_OK){
            selectDB()
        }
    }
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerView = view.itemHeaderView
    }
    class DataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val completedIconView = view.completedIconView
        val itemTitleView = view.itemTitleView
        val itemContentView = view.itemContentView
    }

    inner class MyAdapter(val list: MutableList<ItemVO>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return list.get(position).type
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == ItemVO.TYPE_HEADER) {
                val layoutInflater = LayoutInflater.from(parent?.context)
                return HeaderViewHolder(
                        layoutInflater.inflate(
                                R.layout.item_header,
                                parent,
                                false
                        )
                )
            } else {
                val layoutInflater = LayoutInflater.from(parent?.context)
                return DataViewHolder(
                        layoutInflater.inflate(
                                R.layout.item_main,
                                parent,
                                false
                        )
                )
            }
        }
        override fun getItemCount(): Int {
            return list.size
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemVO = list.get(position)
            if (itemVO.type == ItemVO.TYPE_HEADER) {
                //as .. type casting
                val viewHolder = holder as HeaderViewHolder
                val headerItem = itemVO as HeaderItem
                viewHolder.headerView.setText(headerItem.date)
            } else {
                val viewHolder = holder as DataViewHolder
                val dataItem = itemVO as DataItem
                viewHolder.itemTitleView.setText(dataItem.title)
                viewHolder.itemContentView.setText(dataItem.content)

                if(dataItem.completed){
                    viewHolder.completedIconView.setImageResource(R.drawable.icon_completed)
                }else {
                    viewHolder.completedIconView.setImageResource(R.drawable.icon)
                }

                viewHolder.completedIconView.setOnClickListener{
                    val helper = DB(this@MainActivity)
                    val db=helper.writableDatabase

                    if(dataItem.completed){
                        db.execSQL("update tb_todo set completed=? where _id=?", arrayOf(0, dataItem.id))
                        viewHolder.completedIconView.setImageResource(R.drawable.icon)
                    }else {
                        db.execSQL("update tb_todo set completed=? where _id=?", arrayOf(1, dataItem.id))
                        viewHolder.completedIconView.setImageResource(R.drawable.icon_completed)
                    }
                    dataItem.completed = !dataItem.completed
                    db.close()
                }
            }
        }
    }
    inner class MyDecoration() : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            val index = parent!!.getChildAdapterPosition(view)
            Log.d("kkang","index $index.... list size: ${list.size}")
            val itemVO = list.get(index)
            if (itemVO.type == ItemVO.TYPE_DATA) {
                view!!.setBackgroundColor(0xFFFFFFFF.toInt())
                ViewCompat.setElevation(view, 10.0f)
            }
            outRect!!.set(20, 10, 20, 10)
        }
    }
}
abstract class ItemVO {
    abstract val type: Int
    companion object {
        val TYPE_HEADER = 0
        val TYPE_DATA = 1
    }
}
class HeaderItem(var date: String) : ItemVO() {
    override val type: Int
        get() = TYPE_HEADER
}

internal class DataItem(var id: Int, var title: String, var content: String, var completed: Boolean = false) : ItemVO() {
    override val type: Int
        get() = TYPE_DATA
}

