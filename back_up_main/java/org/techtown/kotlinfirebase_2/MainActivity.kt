package org.techtown.kotlinfirebase_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn_map : ImageView = findViewById<ImageView>(R.id.btn_map)
        val btn_search : Button = findViewById<Button>(R.id.btn_search)
        btn_map.setOnClickListener {
            val intent = Intent(this, Map::class.java)
            startActivity(intent)
        }
        btn_search.setOnClickListener {
            Toast.makeText(this@MainActivity, "검색하기", Toast.LENGTH_SHORT).show()
        }



        val recyclerView : RecyclerView = findViewById(R.id.recyclerview)
        // 파이어스토어 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance()

        recyclerView.adapter = RecyclerViewAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)


    }


    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // Person 클래스 ArrayList 생성성
        var telephoneBook : ArrayList<Person> = arrayListOf()

        init {  // telephoneBook의 문서를 불러온 뒤 Person으로 변환해 ArrayList에 담음
            firestore?.collection("telephoneBook")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // ArrayList 비워줌
                telephoneBook.clear()

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(Person::class.java)
                    telephoneBook.add(item!!)
                }
                notifyDataSetChanged()
            }
        }

        // xml파일을 inflate하여 ViewHolder를 생성
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
            return ViewHolder(view)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name : TextView = itemView.findViewById(R.id.tv_name)
            val phoneNum : TextView = itemView.findViewById(R.id.tv_phoneNum)

            fun bind(item: Person) {
                name.text = item.name
                phoneNum.text = item.phoneNumber

            }
            init {
                itemView.setOnClickListener{

                }
            }
        }

        // onCreateViewHolder에서 만든 view와 실제 데이터를 연결
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as ViewHolder).itemView

            holder.name.text = telephoneBook[position].name
            holder.phoneNum.text = telephoneBook[position].phoneNumber

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView?.context, Post::class.java)
                startActivity(intent)
            }
        }

        // 리사이클러뷰의 아이템 총 개수 반환
        override fun getItemCount(): Int {
            return telephoneBook.size
        }
    }
}
