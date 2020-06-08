
#include <SPI.h>
#include <Servo.h>
#include <MFRC522.h> // thu vien "RFID".
#define IN1 23
#define IN2 2
#define IN3 3
#define IN4 29
#define MAX_SPEED 255 //từ 0-255
#define MIN_SPEED 0
#define SS_PIN 53
#define RST_PIN 5
#define GAS_SENSOR_PIN 2
#define QUAT_PHONG_BEP_PIN 22
#define QUAT_PHONG_NGU_PIN 24
#define QUAT_PHONG_KHACH_PIN 26
#define SERVO1 14 // Cửa sổ bêps
#define SERVO2 15 // Cửa sổ khách
#define SERVO3 16  // Cửa sổ ngủ
#define SERVO4 17
#define SERVO5 18
#define DEN_PHONG_NGU_PIN 13
#define DEN_PHONG_KHACH_PIN 12
#define DEN_GARA_PIN 11
#define DEN_PHONG_BEP_PIN 10
#define DEN_PHONG_TAM_PIN 9
Servo servo1;     // Khai báo chân cho servo
Servo servo2;   
Servo servo3;
Servo servo4;
Servo servo5;
MFRC522 mfrc522(SS_PIN, RST_PIN);       
unsigned long uidDec, uidDecTemp, old_uid; // hien thi so UID dang thap phan
int count;
byte bCounter, readBit;
unsigned long ticketNumber;
String data;
int temp = 0;
void setup() {
  pinMode(DEN_PHONG_NGU_PIN, OUTPUT);
  pinMode(DEN_PHONG_KHACH_PIN, OUTPUT);
  pinMode(DEN_GARA_PIN, OUTPUT);
  pinMode(DEN_PHONG_BEP_PIN, OUTPUT);
  pinMode(DEN_PHONG_TAM_PIN, OUTPUT);
  pinMode(QUAT_PHONG_BEP_PIN, OUTPUT);
  pinMode(QUAT_PHONG_KHACH_PIN, OUTPUT);
  pinMode(QUAT_PHONG_NGU_PIN, OUTPUT);
  digitalWrite(DEN_PHONG_NGU_PIN,HIGH);
  digitalWrite(DEN_PHONG_KHACH_PIN,HIGH);
  digitalWrite(DEN_GARA_PIN,HIGH);
  digitalWrite(DEN_PHONG_BEP_PIN,HIGH);
  digitalWrite(QUAT_PHONG_BEP_PIN,HIGH);
  digitalWrite(QUAT_PHONG_KHACH_PIN,HIGH);
  digitalWrite(QUAT_PHONG_NGU_PIN,HIGH);
  digitalWrite(DEN_PHONG_TAM_PIN,HIGH);
  servo1.attach(SERVO1);
  servo2.attach(SERVO2);
  servo3.attach(SERVO3);
  servo4.attach(SERVO4);
  servo5.attach(SERVO5);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  Serial.begin(9600);     
  SPI.begin();            
  mfrc522.PCD_Init();     
}
void cuacuon_Dung() {
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
}
void cuachinh_Dung() {
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, LOW);
}
void cuacuon_mo(int speed) { //speed: từ 0 - MAX_SPEED
  speed = constrain(speed, MIN_SPEED, MAX_SPEED);//đảm báo giá trị nằm trong một khoảng từ 0 - MAX_SPEED - http://arduino.vn/reference/constrain
  digitalWrite(IN1, HIGH);// chân này không có PWM
  analogWrite(IN2, 255 - speed);
}
 
void cuacuon_dong(int speed) {
  speed = constrain(speed, MIN_SPEED, MAX_SPEED);//đảm báo giá trị nằm trong một khoảng từ 0 - MAX_SPEED - http://arduino.vn/reference/constrain
  digitalWrite(IN1, LOW);// chân này không có PWM
  analogWrite(IN2, speed);
}
void cuachinh_mo(int speed) { //speed: từ 0 - MAX_SPEED
  speed = constrain(speed, MIN_SPEED, MAX_SPEED);//đảm báo giá trị nằm trong một khoảng từ 0 - MAX_SPEED - http://arduino.vn/reference/constrain
  analogWrite(IN3, speed);
  digitalWrite(IN4, LOW);// chân này không có PWM
}
 
void cuachinh_dong(int speed) {
  speed = constrain(speed, MIN_SPEED, MAX_SPEED);//đảm báo giá trị nằm trong một khoảng từ 0 - MAX_SPEED - http://arduino.vn/reference/constrain
  analogWrite(IN3, 255 - speed);
  digitalWrite(IN4, HIGH);// chân này không có PWM
}
void gas(){
    if(analogRead(A8)>500) //Tín hiệu từ cảm biết khí gas
    {
      temp = 1; 
    digitalWrite(QUAT_PHONG_BEP_PIN,LOW); 
    Serial.println("0003");
    servo1.write(180);
    delay(5000);
    }
    if(analogRead(A8)<500 && temp == 1)
    {
    temp = 0;
    digitalWrite(QUAT_PHONG_BEP_PIN,HIGH);    
    servo1.write(0);
    delay(1000);
    }
}
void voice(){
  while (Serial.available()){  //Kiểm tra byte để đọc
    delay(30); //Delay để ổn định hơn 
    char c = Serial.read(); // tiến hành đọc
    if (c == '#') {break;} //Thoát khỏi vòng lặp khi phát hiện từ #
    data += c; // data = data + c
  } 
  if (data.length() > 0) {
    Serial.println(data);  
  }
  if(data == "bật đèn phòng ngủ" || data == "Bật đèn phòng ngủ" ){digitalWrite(DEN_PHONG_NGU_PIN,LOW);}
  if(data == "tắt đèn phòng ngủ" || data == "Tắt đèn phòng ngủ"){digitalWrite(DEN_PHONG_NGU_PIN,HIGH);}
  if(data == "bật đèn phòng tắm" || data == "Bật đèn phòng tắm" ){digitalWrite(DEN_PHONG_TAM_PIN,LOW);}
  if(data == "tắt đèn phòng tắm" || data == "Tắt đèn phòng tắm"){digitalWrite(DEN_PHONG_TAM_PIN,HIGH);}
  if(data == "bật đèn phòng khách" || data == "Bật đèn phòng khách" ){digitalWrite(DEN_PHONG_KHACH_PIN,LOW);}
  if(data == "tắt đèn phòng khách" || data == "Tắt đèn phòng khách"){digitalWrite(DEN_PHONG_KHACH_PIN,HIGH);}
  if(data == "bật đèn phòng bếp" || data == "Bật đèn phòng bếp" ){digitalWrite(DEN_PHONG_BEP_PIN,LOW);}
  if(data == "tắt đèn phòng bếp" || data == "Tắt đèn phòng bếp"){digitalWrite(DEN_PHONG_BEP_PIN,HIGH);}
  if(data == "bật đèn gara ô tô" || data == "Bật đèn gara ô tô" ){digitalWrite(DEN_GARA_PIN,LOW);}
  if(data == "tắt đèn gara ô tô" || data == "Tắt đèn gara ô tô"){digitalWrite(DEN_GARA_PIN,HIGH);}
  if(data == "bật quạt phòng ngủ" || data == "Bật quạt phòng ngủ" ){digitalWrite(QUAT_PHONG_NGU_PIN,LOW);}
  if(data == "tắt quạt phòng ngủ" || data == "Tắt quạt phòng ngủ"){digitalWrite(QUAT_PHONG_NGU_PIN,HIGH);}
  if(data == "bật quạt phòng khách" || data == "Bật quạt phòng khách" ){digitalWrite(QUAT_PHONG_KHACH_PIN,LOW);}
  if(data == "tắt quạt phòng khách" || data == "Tắt quạt phòng khách"){digitalWrite(QUAT_PHONG_KHACH_PIN,HIGH);}
  if(data == "bật quạt phòng bếp" || data == "Bật quạt phòng bếp" ){digitalWrite(QUAT_PHONG_BEP_PIN,LOW);}
  if(data == "tắt quạt phòng bếp" || data == "Tắt quạt phòng bếp"){digitalWrite(QUAT_PHONG_BEP_PIN,HIGH);}
  if(data == "Quẩy lên đi" || data == "quẩy lên đi"){
    digitalWrite(DEN_PHONG_NGU_PIN,LOW);
    digitalWrite(DEN_PHONG_TAM_PIN,LOW);
    digitalWrite(DEN_PHONG_KHACH_PIN,LOW);
    digitalWrite(DEN_GARA_PIN,LOW);
    digitalWrite(DEN_PHONG_BEP_PIN,LOW);}
  if(data == "mở cửa sổ phòng bếp" || data == "Mở cửa sổ phòng bếp"){
    servo1.write(180);
    delay(1000);
    }
  if(data == "đóng cửa sổ phòng bếp" || data == "Đóng cửa sổ phòng bếp"){
    servo1.write(0);
    delay(1000);
    }
  if(data == "mở cửa sổ phòng khách" || data == "Mở cửa sổ phòng khách"){
    servo2.write(180);
    delay(1000);
    }
  if(data == "đóng cửa sổ phòng khách" || data == "Đóng cửa sổ phòng khách"){
    servo2.write(0);
    delay(1000);
    }
  if(data == "mở cửa sổ phòng ngủ" || data == "Mở cửa sổ phòng ngủ"){
    servo3.write(180);
    delay(1000);
    }
  if(data == "đóng cửa sổ phòng ngủ" || data == "Đóng cửa sổ phòng ngủ"){
    servo3.write(0);
    delay(1000);
    }
  if(data == "mở cửa gara ô tô" || data == "Mở cửa gara ô tô"){
    //cuacuon_mo(150);
    servo4.write(180);
    delay(1000);
    //cuacuon_Dung();
    }
   if(data == "đóng cửa gara ô tô" || data == "Đóng cửa gara ô tô"){
    //cuacuon_dong(150);
    servo4.write(0);
    delay(1000);
    //cuacuon_Dung();
    }
   if(data == "mở cửa chính" || data == "Mở cửa chính"){
    cuachinh_mo(150);
    servo5.write(180);
    delay(1000);
    //cuachinh_Dung();
    }
   if(data == "đóng cửa chính" || data == "Đóng cửa chính"){
    servo5.write(0);
    delay(1000);
    //cuachinh_Dung();
    } 
  data="";
}
void loop() {
  voice();
  gas();
  if ( ! mfrc522.PICC_IsNewCardPresent()) {
    return;
  }
  if ( ! mfrc522.PICC_ReadCardSerial()) {
    return;
  }
  uidDec = 0;
  count++;
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    uidDecTemp = mfrc522.uid.uidByte[i];
    uidDec = uidDec*256+uidDecTemp;
  }
  if(count == 1){
      if(uidDec == 1989414682){
        Serial.println("0001");
        //cuachinh_mo(100);
        servo5.write(180);
        delay(2000);
        //cuachinh_Dung();
        
        }else{
          Serial.println("0002");
        }        
  }
  if(count==2){
      delay(1000);
      count =0;
    }
  old_uid = uidDec;
}
