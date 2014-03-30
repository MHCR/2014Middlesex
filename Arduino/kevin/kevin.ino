
#include <Adafruit_NeoPixel.h>

#define LEFT_STRIP_LED 5
#define RIGHT_STRIP_LED 8

Adafruit_NeoPixel leftStrip = Adafruit_NeoPixel(19, LEFT_STRIP_LED, NEO_GRB + NEO_KHZ800);
Adafruit_NeoPixel rightStrip = Adafruit_NeoPixel(19, RIGHT_STRIP_LED, NEO_GRB + NEO_KHZ800);

void setup() {
  Serial.begin(9600);
  leftStrip.begin();
  rightStrip.begin();
  leftStrip.show(); // Initialize all pixels to 'off'
  rightStrip.show(); // Initialize all pixels to 'off'
}

void loop() {
  colorWipeCool();
}

void showPixels() {
  leftStrip.show();
  rightStrip.show();
}

double seed = 1;
double wipeSeed = 19;
int j = 0;

void colorWipeCool() {
  int i = seed;
  j = 0;
  for(j; j <= 19;j++){ 
   for(i; i != seed - 1; seed++){       
      leftStrip.setPixelColor(i, 255 / i, 255 - 255/i, 255/i);
      rightStrip.setPixelColor(i, 255 / i, 255 - 255/i, 255/i);
      showPixels();      
      delay(5);
      if(i == 19){
        i = 0;
      }
     }
    }
    if(seed == 19){
     seed = 0; 
    }else{
     seed = seed + 1;
    }
  }
  
   
  

