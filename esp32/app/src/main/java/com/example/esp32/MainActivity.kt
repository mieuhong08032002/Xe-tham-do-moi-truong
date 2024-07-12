package com.example.esp32

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Base64
import android.util.Log
import java.net.URLDecoder
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity(),View.OnClickListener,View.OnTouchListener {
    lateinit var txtTemperature:TextView
    lateinit var txtHumanity:TextView
    lateinit var txtCo2:TextView
    lateinit var btn_phai:Button
    lateinit var btn_trai:Button
    lateinit var btn_len:Button
    lateinit var btn_xuong:Button
    lateinit var btn_dv_t:ImageView
    lateinit var btn_dv_l:ImageView
    lateinit var btn_dv_tr:ImageView
    lateinit var btn_dv_ph:ImageView
    lateinit var btn_light:ImageView
    lateinit var btn:Button
    lateinit var img_v:ImageView
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    lateinit var myRef: DatabaseReference
    lateinit var myRef_r: DatabaseReference
    var servo:Int=0
    var sevor2: Int=0
    var led: Int =0
    private var isFunctionCalled = false
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var id_Bnt:List<Int> = listOf(R.id.btn_sv_left,R.id.btn_Light,R.id.btn_sv_right,R.id.btn_del_wifi,R.id.btn_sv_up,R.id.btn_sv_down,R.id.btn_up)
        var id_Btn_Touch:List<Int> = listOf(R.id.btn_up,R.id.btn_down,R.id.btn_left,R.id.btn_right )
        btn_phai= findViewById(R.id.btn_sv_right)
        btn_trai= findViewById(R.id.btn_sv_left)
        btn_len= findViewById(R.id.btn_sv_up)
        btn_xuong= findViewById(R.id.btn_sv_down)
        btn_dv_l= findViewById(R.id.btn_down)
        btn_dv_t= findViewById(R.id.btn_up)
        btn_dv_ph= findViewById(R.id.btn_right)
        btn_dv_tr= findViewById(R.id.btn_left)
        btn=findViewById(R.id.btn_del_wifi)
        img_v= findViewById(R.id.imgb64)
        txtTemperature= findViewById(R.id.txt_Temp)
        txtHumanity= findViewById(R.id.txt_Humi)
        txtCo2= findViewById(R.id.txt_Co2)
        btn_light= findViewById(R.id.btn_Light)
        loadData()
        get_data_cam()
        get_data_sensor()
        id_Bnt.forEachIndexed { index, it ->
            val v = findViewById<View>(it)
            v.setOnClickListener(this)
        }
        id_Btn_Touch.forEachIndexed{index, i ->
            val v = findViewById<View>(i)
            v.setOnTouchListener(this)
        }
    }
    private fun loadData(){
        if(!isFunctionCalled){
            myRef = database.getReference("/esp32-cam/photo")
            myRef.setValue("0")
            set_Data_light_fb(0)
        }
    }
    private fun get_data_cam(){
        myRef_r = database.reference.child("/esp32-cam/photo")
        myRef_r.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Xử lý dữ liệu khi có sự thay đổi
                val dataValue = dataSnapshot.getValue(String::class.java)
                dataValue?.let { set_img_b64(it) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý lỗi khi có lỗi xảy ra
            }
        })
    }
    private fun get_data_sensor(){
        myRef_r = database.reference.child("/Data_device/user1/data_sensor")
        myRef_r.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Xử lý dữ liệu khi có sự thay đổi
                val dataValue = dataSnapshot.getValue(String::class.java)
                dataValue?.let {
                    val parts = dataValue.split(":") // Tách chuỗi bằng dấu cách
                    txtTemperature.text = parts[0] + " °C"
                    txtHumanity.text= parts[1] + " %"
                    txtCo2.text= parts[2] + " PPM"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý lỗi khi có lỗi xảy ra
            }
        })
    }
    private fun set_img_b64(img_b64:String){
        if (img_b64 == "0"){
            img_v.setImageDrawable(getDrawable(R.drawable.pngegg))
            return
        }
        val decodedUrl = URLDecoder.decode(img_b64, "UTF-8")
        // Giải mã chuỗi Base64 thành một mảng byte
        val decodedString: ByteArray = Base64.decode(decodedUrl, Base64.DEFAULT)
        // Tạo bitmap từ mảng byte
        val decodedByte: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        // Đặt hình ảnh đã giải mã lên ImageView
        img_v.setImageBitmap(decodedByte)


    }
    private fun set_Data_light_fb(stt_led:Int) {
        var img_led:List<Int> = listOf(R.drawable.light_off,R.drawable.light_on)
        myRef = database.getReference("Data_device/user1/dv1")
        myRef.setValue(stt_led+6)
            .addOnSuccessListener {
               btn_light.setImageDrawable(getDrawable(img_led[stt_led]))
            }
            .addOnFailureListener {
                // Xử lý khi gửi giá trị thất bại
            }
    }
    private fun set_Data_dv_fb(control_dv:Int) {
        myRef = database.getReference("Data_device/user1/dv1")
        myRef.setValue(control_dv)
            .addOnSuccessListener {
                if(control_dv == 5 ){ Toast.makeText(this, " Đã xóa WiFI thành công !", Toast.LENGTH_LONG).show()}
            }
            .addOnFailureListener {
                // Xử lý khi gửi giá trị thất bại
            }
    }
    private fun set_Data_sv1_fb(servoValue:Int) {
        myRef= database.getReference("Data_device/user1/sv1")
        myRef.setValue(servoValue)
            .addOnSuccessListener {
                //Toast.makeText(this, "success", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                // Xử lý khi gửi giá trị thất bại
            }
    }
    private fun set_Data_sv2_fb(servoValue:Int) {
        myRef= database.getReference("Data_device/user1/sv2")
        myRef.setValue(servoValue)
            .addOnSuccessListener {
                //Toast.makeText(this, "success", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                // Xử lý khi gửi giá trị thất bại
            }
    }
    override fun onClick(v: View) {
        when(v.id){
            R.id.btn_sv_left -> kotlin.run {
                servo = if (servo>=180) 180 else servo + 10
                set_Data_sv1_fb(servo) }
            R.id.btn_sv_right -> kotlin.run {
                servo = if (servo <= 0) 0 else servo - 10
                set_Data_sv1_fb(servo) }
            R.id.btn_sv_down -> kotlin.run {
                sevor2 = if (sevor2>=90) 90 else sevor2 + 5
                set_Data_sv2_fb(sevor2) }
            R.id.btn_sv_up -> kotlin.run {
                sevor2 = if (sevor2 <= 0) 0 else sevor2 - 5
                set_Data_sv2_fb(sevor2) }
            R.id.btn_Light -> kotlin.run { led =(led+1)%2
                set_Data_light_fb(led) }
            R.id.btn_del_wifi -> kotlin.run { showMyDialog() }
        }
    }
    override fun onTouch(v: View, m: MotionEvent): Boolean {
        when(m.action){
            MotionEvent.ACTION_DOWN -> {
                when(v.id){
                    R.id.btn_up -> kotlin.run { set_Data_dv_fb(2)
                        btn_dv_t.setImageDrawable(getDrawable(R.drawable.btn_t_dark))
                    }
                    R.id.btn_down  -> kotlin.run { set_Data_dv_fb(4)
                        btn_dv_l.setImageDrawable(getDrawable(R.drawable.btn_d_dark))
                    }
                    R.id.btn_left -> kotlin.run { set_Data_dv_fb(3)
                        btn_dv_tr.setImageDrawable(getDrawable(R.drawable.btn_l_dark))
                    }
                    R.id.btn_right -> kotlin.run { set_Data_dv_fb(1)
                        btn_dv_ph.setImageDrawable(getDrawable(R.drawable.btn_r_dark))
                    }
                }
            true
            }
            MotionEvent.ACTION_UP ->{
                set_Data_dv_fb(0)
                when(v.id){
                    R.id.btn_up -> kotlin.run { btn_dv_t.setImageDrawable(getDrawable(R.drawable.btn_top)) }
                    R.id.btn_down  -> kotlin.run { btn_dv_l.setImageDrawable(getDrawable(R.drawable.btn_bottom))                 }
                    R.id.btn_left -> kotlin.run { btn_dv_tr.setImageDrawable(getDrawable(R.drawable.btn_left)) }
                    R.id.btn_right -> kotlin.run { btn_dv_ph.setImageDrawable(getDrawable(R.drawable.btn_right)) }
                }
            true
            }
            else -> false
        }
        return true
    }
    private fun showMyDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle("Thông báo")
        alertDialogBuilder.setMessage("Bạn có chắc chắn muốn xóa dữ liệu WIFI ! ")

        // Nút tích cực
        alertDialogBuilder.setPositiveButton("Đồng ý") { dialog, _ ->
            // Xử lý khi người dùng nhấn nút "Đồng ý"
            set_Data_dv_fb(5)
            dialog.dismiss()
        }
        // Nút tiêu cực
        alertDialogBuilder.setNegativeButton("Hủy bỏ") { dialog, _ ->
            // Xử lý khi người dùng nhấn nút "Hủy bỏ"
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()

        // Hiển thị hộp thoại
        alertDialog.show()
    }
}

