const int motorPin1 = 12; // Chân điều khiển motor 1 (ENA)
const int motorPin3 = 14; // Chân điều khiển motor 1 (IN1)
const int motorPin4 = 27; // Chân điều khiển motor 1 (IN2)
const int motorPin5 = 26; // Chân điều khiển motor 2 (IN3)
const int motorPin6 = 25; // Chân điều khiển motor 2 (IN4)
const int motorPin2 = 33; // Chân điều khiển motor 2 (ENB)

#include <ESP32Servo.h>
#include "FirebaseESP32.h"
#include <WiFiManager.h>
#include <ArduinoJson.h>
#include <MQ135.h>
#include <DHT.h>

#define FIREBASE_HOST "esp32-cam-servo-default-rtdb.asia-southeast1.firebasedatabase.app/"
#define FIREBASE_AUTH "tZbfdHMHExOzaDjch5nshvsVxosIw8vdR5mQoet4"
// cài đặt PWM=
const int freq = 5000;  // tần số xung
const int ledChannel = 3; // kênh PWM
const int resolution = 10; // độ phân giải 8bit
int speed_d;
#define PIN_MQ135 35 // MQ135 Analog Input Pin
#define DHTPIN 5 // DHT Digital Input Pin
#define DHTTYPE DHT11 // DHT11 or DHT22, depends on your sensor
#define LED_PIN 13
#define LED_ON() digitalWrite(LED_PIN, HIGH)
#define LED_OFF() digitalWrite(LED_PIN, LOW)
MQ135 mq135_sensor(PIN_MQ135);
DHT dht(DHTPIN, DHTTYPE);
FirebaseData fb;
String path = "/";
FirebaseJson json;

Servo servoMotor;
Servo servoMotor2;
int servo_old = 0 ;
int servo_old1 = 0 ;

WiFiManager wifiManager;

String data_Sensor;

void setup() {
  pinMode(LED_PIN, OUTPUT);
   
  pinMode(motorPin1, OUTPUT);
  pinMode(motorPin2, OUTPUT);
  pinMode(motorPin3, OUTPUT);
  pinMode(motorPin4, OUTPUT);
  pinMode(motorPin5, OUTPUT);
  pinMode(motorPin6, OUTPUT);
  digitalWrite(LED_PIN,1);
  ledcSetup(ledChannel, freq, resolution);
  ledcAttachPin(motorPin1, ledChannel);
  ledcAttachPin(motorPin2, ledChannel);
  Serial.begin(115200);
  
  dht.begin();
  
  servoMotor.attach(18);
  servoMotor2.attach(19);

  // reset và erase cài đặt trước đó
  // wifiManager.resetSettings();
  // tên thiết bị, để tạo SSID mặc định
  wifiManager.autoConnect("ESP32");

  // Sau khi kết nối thành công
  Serial.println("Kết nối WiFi thành công!");
  //firebase
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}

void loop() {
  get_data_sesor_sand_sever();
  //Firebase.getInt(fb, path + "/data/servo");
  Firebase.getInt(fb, path + "/Data_device/user1/sv1"); //lấy gtri từ Firebase về gán vào biến x
  // stt_dv[0] = fb.intData(); //lay gtri tu fb ve esp
  if (servo_old != fb.intData()) {
    Serial.println(fb.intData());
    servoMotor.write(fb.intData());
    servo_old = fb.intData();
  }
  delay(100);
  Firebase.getInt(fb, path + "/Data_device/user1/sv2"); //lấy gtri từ Firebase về gán vào biến x
  if (servo_old1 != fb.intData()) {
    Serial.println(fb.intData());
    servoMotor2.write(fb.intData());
    servo_old1 = fb.intData();
  }
  Firebase.getInt(fb, path + "/Data_device/user1/speed"); //lấy gtri từ Firebase về gán vào biến x
  speed_d=fb.intData();
  
  Firebase.getInt(fb, path + "/Data_device/user1/dv1"); //lấy gtri từ Firebase về gán vào biến x
  int rec_fb = fb.intData();
  delay(100);
  Serial.println(rec_fb);
  // int speed_fb =   Firebase.getInt(fb, path + "/Data_device/user1/speed"); //lấy gtri từ Firebase về gán vào biến x
  control_Car(rec_fb);
}
void control_Car(int data_fb){
  if (data_fb == 1) { smoothMoveForward();return;}
  else if (data_fb == 2) {smoothTurnRight() ; return;}
  else if (data_fb == 3) {smoothMoveBackward();return;}
  else if (data_fb == 4) {smoothTurnLeft();  return;}
  else if (data_fb == 5) {Firebase.setInt(fb, path + "/Data_device/user1/dv1", 0); wifiManager.resetSettings(); return;}
  else if(data_fb == 6){LED_OFF();return;}
  else if(data_fb == 7){LED_ON(); return;}
  else { stop_car();}
  }
void get_data_sesor_sand_sever() {
  float temperature, humidity;
  humidity = dht.readHumidity();
  temperature = dht.readTemperature();
  // Check if any reads failed and exit early (to try again).
  if (isnan(humidity) || isnan(temperature)) {
    Serial.println(F("Failed to read from DHT sensor!"));
    return;
  }
   float ppm = mq135_sensor.getPPM();
   data_Sensor = (String)(temperature)+ ":"+(String)(humidity)+":"+(String)(ppm);
   Serial.println(data_Sensor);
   Firebase.setString(fb, path + "/Data_device/user1/data_sensor",data_Sensor);
   delay(100);
  }
void smoothMoveForward() {
  ledcWrite(ledChannel,speed_d);
  // for (int i = 0 ; i < speed; i++) {
  // analogWrite(motorPin1, i * 4); // PWM cho motor 1
  //analogWrite(motorPin2, i * 4); // PWM cho motor 2
  digitalWrite(motorPin3, HIGH); // Điều khiển motor 1 tiến
  digitalWrite(motorPin4, LOW); // Điều khiển motor 1 tiến
 // digitalWrite(motorPin5, HIGH); // Điều khiển motor 2 tiến
  //digitalWrite(motorPin6, LOW); // Điều khiển motor 2 tiến
  // }
}

// Hàm điều khiển xe robot lùi mượt
void smoothMoveBackward() {
  ledcWrite(ledChannel,speed_d);
  // for (int i = 0; i <= speed; i++) {
  //analogWrite(motorPin1, i * 4); // PWM cho motor 1
  //analogWrite(motorPin2, i * 4); // PWM cho motor 2
  digitalWrite(motorPin3, LOW); // Điều khiển motor 1 lùi
  digitalWrite(motorPin4, HIGH); // Điều khiển motor 1 lùi
  //digitalWrite(motorPin5, LOW); // Điều khiển motor 2 lùi
  //digitalWrite(motorPin6, HIGH); // Điều khiển motor 2 lùi
  //  }
}

// Hàm điều khiển xe robot rẽ phải mượt
void smoothTurnRight() {
  ledcWrite(ledChannel,speed_d);
  // for (int i = 0; i <= speed; i++) {
  //analogWrite(motorPin1, i * 4); // PWM cho motor 1
  //analogWrite(motorPin2, i * 4); // PWM cho motor 2
  //digitalWrite(motorPin3, LOW); // Điều khiển motor 1 lùi
  //digitalWrite(motorPin4, HIGH); // Điều khiển motor 1 lùi
  digitalWrite(motorPin5, HIGH); // Điều khiển motor 2 tiến
  digitalWrite(motorPin6, LOW); // Điều khiển motor 2 tiến
   delay(5);
  // }
}

// Hàm điều khiển xe robot rẽ trái mượt
void smoothTurnLeft() {
  ledcWrite(ledChannel,speed_d);
  // for (int i = 0; i <= speed; i++) {
  //analogWrite(motorPin1, i * 4); // PWM cho motor 1
  //analogWrite(motorPin2, i * 4); // PWM cho motor 2
  //digitalWrite(motorPin3, HIGH); // Điều khiển motor 1 tiến
  //digitalWrite(motorPin4, LOW); // Điều khiển motor 1 tiến
  digitalWrite(motorPin5, LOW); // Điều khiển motor 2 lùi
  digitalWrite(motorPin6, HIGH); // Điều khiển motor 2 lùi
  delay(5);
  // }
}
void stop_car(){
  digitalWrite(motorPin3, LOW); // Điều khiển motor 1 tiến
  digitalWrite(motorPin4, LOW); // Điều khiển motor 1 tiến
  digitalWrite(motorPin5, LOW); // Điều khiển motor 2 tiến
  digitalWrite(motorPin6, LOW); // Điều khiển motor 2 tiến
  }
