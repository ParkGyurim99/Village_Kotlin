package org.techtown.kotlinfirebase_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import org.techtown.kotlinfirebase_2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    var firestore : FirebaseFirestore? = null
    val db = FirebaseFirestore.getInstance()
    private val TAG = "Firestore"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val btn_map : ImageView = findViewById<ImageView>(R.id.btn_map)
        val btn_search : Button = findViewById<Button>(R.id.btn_search)
        val searchWord : EditText = findViewById<EditText>(R.id.searchWord)
        val btn_write : Button = findViewById<Button>(R.id.btn_write)
        var searchOption = "name"
        val recyclerView : RecyclerView = findViewById(R.id.recyclerview)
        btn_map.setOnClickListener {
            val intent = Intent(this, Map::class.java)
            startActivity(intent)
        }
        btn_search.setOnClickListener {
            (recyclerView.adapter as RecyclerViewAdapter).search(searchWord.text.toString(), searchOption)
        }
        // 파이어스토어 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance()

        recyclerView.adapter = RecyclerViewAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)

        binding.recyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerview.adapter = RecyclerViewAdapter()

        binding.btnWrite.setOnClickListener {
            // 대화상자 생성
            val builder = AlertDialog.Builder(this)
            Log.d(TAG,"%%%%%%%%%%%%%%%%%")
            // 대화상자에 텍스트 입력 필드 추가, 대충 만들었음
            val tvName = TextView(this)
            tvName.text = "Name"
            val tvNumber = TextView(this)
            tvNumber.text = "Number"
            val etName = EditText(this)
            etName.isSingleLine = true
            val etNumber = EditText(this)
            etNumber.isSingleLine = true
            val mLayout = LinearLayout(this)
            mLayout.orientation = LinearLayout.VERTICAL
            mLayout.setPadding(16)
            mLayout.addView(tvName)
            mLayout.addView(etName)
            mLayout.addView(tvNumber)
            mLayout.addView(etNumber)
            builder.setView(mLayout)

            builder.setTitle("데이터 추가")
            builder.setPositiveButton("추가") { dialog, which ->
                // EditText에서 문자열을 가져와 hashMap으로 만듦
                val data = hashMapOf(
                    "name" to etName.text.toString(),
                    "phoneNumber" to etNumber.text.toString()
                )
                // Contacts 컬렉션에 data를 자동 이름으로 저장
                db.collection("telephoneBook")
                    .add(data)
                    .addOnSuccessListener {
                        // 성공할 경우
                        Toast.makeText(this, "데이터가 추가되었습니다", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        // 실패할 경우
                        Log.w("MainActivity", "Error getting documents: $exception")
                    }
            }
            builder.setNegativeButton("취소") { dialog, which ->
            }
            builder.show()
        }
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
        fun search(serachWord : String, option : String) {
            firestore?.collection("telephoneBook")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // ArrayList 비워줌
                telephoneBook.clear()

                for (snapshot in querySnapshot!!.documents) {
                    if (snapshot.getString(option)!!.contains(serachWord)) {
                        var item = snapshot.toObject(Person::class.java)
                        telephoneBook.add(item!!)
                    }
                }
                notifyDataSetChanged()
            }
        }

    }
}
