#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
 
#define OLED_Address 0x3C
Adafruit_SSD1306 oled(1);
String texto ="";
void setup() {
  oled.begin(SSD1306_SWITCHCAPVCC, OLED_Address);
  Serial.begin(9600);
}
 
void loop() {
  if(Serial.available()>0)
    texto = "";
    
  while(Serial.available()>0){
    char c = Serial.read();
    texto += c;
     
    Serial.println(texto);
  }
  
  oled.clearDisplay();
  oled.setTextColor(WHITE);
  oled.setTextSize(1);
  oled.setCursor(0,0); 
  oled.println(texto);
  oled.display();
  delay(100);
}
