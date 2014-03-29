#include <SPI.h>
#include <Dhcp.h>
#include <Dns.h>
#include <Math.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include <EthernetUdp.h>
#include <util.h>
#include <Adafruit_NeoPixel.h>

//digital I/O pin definitions
#define SERIAL_RX 0
#define SERIAL_TX 1
#define COUNT 2
#define PULSE 3
#define SDCARD_SS 4
#define LEFT_STRIP_LED 5
#define LEFT_LED 6
#define RIGHT_LED 7
#define RIGHT_STRIP_LED 8
#define ON_BOARD_LED 9
#define ETHERNET_SS 10
#define ETHERNET_MOSI 11
#define ETHERNET_MISO 12
#define ETHERNET_SCK 13

//analog I/O pin definitions
#define SEARCH A0 //A0
#define FOUND A1 //A1
#define SHOOT A2 //A2
#define RED A3 //A3
#define GREEN A4 //A4
#define BLUE A5 //A5

Adafruit_NeoPixel leftStrip = Adafruit_NeoPixel(19, LEFT_STRIP_LED, NEO_GRB + NEO_KHZ800);
Adafruit_NeoPixel rightStrip = Adafruit_NeoPixel(19, RIGHT_STRIP_LED, NEO_GRB + NEO_KHZ800);
Adafruit_NeoPixel left = Adafruit_NeoPixel(1, LEFT_LED, NEO_GRB + NEO_KHZ800);
Adafruit_NeoPixel right = Adafruit_NeoPixel(1, RIGHT_LED, NEO_GRB + NEO_KHZ800);

//ethernet arduino info
byte mac[] = { 0x90, 0xA2, 0xDA, 0x0E, 0x40, 0xE6 };
IPAddress ip( 10, 8, 69, 100 );
IPAddress dnServer( 10, 8, 69, 1 );
IPAddress gateway( 10, 8, 69, 1 );
IPAddress subnet( 255, 0, 0, 0 );
EthernetServer server(80);

int digitalCount = LOW;
int digitalPulse = LOW;
int digitalSearch = LOW;
int digitalFound = LOW;
int digitalShoot = LOW;
int digitalRed = LOW;
int digitalGreen = LOW;
int digitalBlue = LOW;
uint16_t brightness = 255;
uint16_t wipe = leftStrip.numPixels();
uint16_t bounceBack = false;
uint16_t removing = false;
uint8_t wait = 0;
int countDown = 11;
boolean countUp = true;
uint8_t red = 0;
uint8_t green = 0;
uint8_t blue = 0;
boolean flashOn = false;
int startMode = 0;

void setup() {
  Serial.begin(9600);
  // disable the SD card by switching pin 4 high
  // not using the SD card in this program, but if an SD card is left in the socket,
  // it may cause a problem with accessing the Ethernet chip, unless disabled
  pinMode(4, OUTPUT);
  digitalWrite(4, HIGH);
  Ethernet.begin(mac,ip,dnServer,gateway,subnet);
  server.begin();
  Serial.print("server is at ");
  Serial.println(Ethernet.localIP());
  pinMode(COUNT, INPUT);
  pinMode(PULSE, INPUT);
  pinMode(SEARCH, INPUT);
  pinMode(FOUND, INPUT);
  pinMode(SHOOT, INPUT);
  pinMode(RED, INPUT);
  pinMode(GREEN, INPUT);
  pinMode(BLUE, INPUT);
  pinMode(ON_BOARD_LED, OUTPUT);
  leftStrip.begin();
  rightStrip.begin();
  left.begin();
  right.begin();
  leftStrip.show(); // Initialize all pixels to 'off'
  rightStrip.show(); // Initialize all pixels to 'off'
  left.show(); // Initialize all pixels to 'off'
  right.show(); // Initialize all pixels to 'off'
}

void loop() {
  digitalCount = digitalRead(COUNT);
  digitalPulse = digitalRead(PULSE);
  digitalSearch = digitalRead(SEARCH);
  digitalFound = digitalRead(FOUND);
  digitalShoot = digitalRead(SHOOT);
  digitalRed = digitalRead(RED);
  digitalGreen = digitalRead(GREEN);
  digitalBlue = digitalRead(BLUE);
  if(wait>0) {
    --wait;
    delay(1);
  } else {
    if(digitalCount == HIGH) {
      setMode(1);
      brightness = 255;
      count();
    } else {
      if(wipe==leftStrip.numPixels()) {
        if(digitalRed==HIGH) {
          red = 255;
        } else {
          red = 0;
        }
        if(digitalGreen==HIGH) {
          green = 255;
        } else {
          green = 0;
        }
        if(digitalBlue==HIGH) {
          blue = 255;
        } else {
          blue = 0;
        }
      }
      if(brightness<255) {
        ++brightness;
      }
      if(digitalShoot==HIGH) {
        setMode(2);
        brightness = 255;
        colorWipe(red,green,blue);
        wait = 50;
      } else if(digitalRed==LOW&&digitalGreen==LOW&&digitalBlue==LOW) {
        setMode(3);
        off();
        wait = 50;
      } if(digitalSearch == HIGH) {
        setMode(4);
        colorWipe(red,green,blue);
        wait = 50;
      } else if(digitalFound == HIGH) {
        setMode(5);
        flash(red,green,blue);
        wait = 250;
      } else if(digitalShoot==LOW && (digitalRed==HIGH||digitalGreen==HIGH||digitalBlue==HIGH) && digitalSearch ==LOW && digitalFound==LOW){
        setMode(6);
        //setColor(red,green,blue);
        colorWipeCool();
        wait = 50;
      }
    }
  }
  
  //TODO: save camera image from http://10.8.69.11/axis-cgi/jpg/image.cgi
  
  printHtml();
}

void setMode(int mode) {
  Serial.print("mode: ");
  Serial.println(mode);
  if(mode!=startMode) {
    wipe = leftStrip.numPixels();
    countDown = 11;
    countUp = true;
    flashOn = false;
    startMode = mode;
    if(mode==2) {
      off();
    }
  }
}

void showPixels() {
  leftStrip.show();
  rightStrip.show();
  left.show();
  right.show();
}

void takeShot() {
  brightness = 0;
}

void setBrightness() {
  leftStrip.setBrightness(brightness);
  rightStrip.setBrightness(brightness);
  left.setBrightness(brightness);
  right.setBrightness(brightness);
}

void setColor(uint8_t r, uint8_t g, uint8_t b) {
  setBrightness();
  left.setPixelColor(0,r,g,b);
  right.setPixelColor(0,r,g,b);
  setStripColor(r,g,b);
  showPixels();
}

void setStripColor(uint8_t r, uint8_t g, uint8_t b) {
  for (uint16_t i = 0; i < leftStrip.numPixels(); i++) {
    leftStrip.setPixelColor(i,r,g,b);
    rightStrip.setPixelColor(i,r,g,b);
  }
}

void off() {
  brightness = 0;
  setColor(0,0,0);
}

void count() {
  //if we are looking for a down then when we get a down count down and then look for an up
  if(countUp) {
    if(digitalPulse == HIGH) {
      countDownLights();
      //dont wait we want to get the next signal asap
    } else {
      off();
      --countDown;
      countUp = false;
      wait = 500; //turn off a half a second
    }
  } else {
    if(digitalPulse == HIGH) {
      off();
      --countDown;
      countUp = true;
      wait = 500; //turn off a half a second
    } else {
      countDownLights();
      //dont wait we want to get the next signal asap
    }
  }
}

void countDownLights() {
  switch(countDown) {
    case 10:
      setColor(255,255,255);
      break;
    case 9:
      setColor(255,128,255);
      break;
    case 8:
      setColor(255,0,255);
      break;
    case 7:
      setColor(0,0,255);
      break;
    case 6:
      setColor(0,255,255);
      break;
    case 5:
      setColor(0,255,0);
      break;
    case 4:
      setColor(128,255,0);
      break;
    case 3:
      setColor(255,255,0);
      break;
    case 2:
      setColor(255,128,0);
      break;
    case 1:
      setColor(255,0,0);
      break;
    default:
      setColor(0,0,0);
      countDown=11;
      break;
  }
}

// Fill the dots one after the other with a color
void colorWipe(uint8_t r, uint8_t g, uint8_t b) {
  setBrightness();
  left.setPixelColor(0, r, g, b);
  right.setPixelColor(0, r, g, b);
  if(wipe>=0) {
    leftStrip.setPixelColor(wipe, r, g, b);
    rightStrip.setPixelColor(wipe, r, g, b);
    --wipe;
  }
  showPixels();
}
double seed = 0.1;
double wipeSeed = 19;

void colorWipeCool() {
  int color = sin(seed) * 255;
  seed >= PI * 2 ? 0.1: seed + ((PI * 2.0) / 10.0);
  seed = seed + 1;
  
  setBrightness();
  if(wipeSeed>=0) {
    leftStrip.setPixelColor(wipeSeed, color, color, color);
    rightStrip.setPixelColor(wipeSeed, 255 - color,255 - color,  255 - color);
    if(wipeSeed == 0){
      wipeSeed = 19;
    }else{
    --wipeSeed;
    }
  }
  showPixels();
}

//TODO: bounce back and forth on the strip
//void colorBounce(uint8_t r, uint8_t g, uint8_t b) {
//  setBrightness();
//  left.setPixelColor(0, r, g, b);
//  right.setPixelColor(0, r, g, b);
//  if(wipe<leftStrip.numPixels()) {
//    leftStrip.setPixelColor(wipe, r, g, b);
//    rightStrip.setPixelColor(wipe, r, g, b);
//    ++wipe;
//  } else {
//    setStripColor(0,0,0);
//    wipe = 0;
//  }
//  showPixels();
//}

void flash(uint8_t r, uint8_t g, uint8_t b) {
  if(flashOn) {
    setColor(r,g,b);
  } else {
    setColor(0,0,0);
  }
  flashOn = !flashOn;
}

void printHtml() {
  // listen for incoming clients
  EthernetClient client = server.available();
  if (client) {
    Serial.println("new client");
    // an http request ends with a blank line
    boolean currentLineIsBlank = true;
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        Serial.write(c);
        // if you've gotten to the end of the line (received a newline
        // character) and the line is blank, the http request has ended,
        // so you can send a reply
        if (c == '\n' && currentLineIsBlank) {
          client.println("HTTP/1.1 200 OK\nContent-Type: text/html\nConnection: close\nRefresh: 5\n\n<!DOCTYPE HTML>\n<html>");
          client.println("<table>");
          for(int i=0;i<=20;++i) {
            client.print("<tr>\n<td>");
            switch(i) {
              case 0:
                client.print("variable");
                break;
              case 1:
                client.print("count");
                break;
              case 2:
                client.print("pulse");
                break;
              case 3:
                client.print("search");
                break;
              case 4:
                client.print("found");
                break;
              case 5:
                client.print("shoot");
                break;
              case 6:
                client.print("color1");
                break;
              case 7:
                client.print("color2");
                break;
              case 8:
                client.print("color3");
                break;
              case 9:
                client.print("brightness");
                break;
              case 10:
                client.print("wipe");
                break;
              case 11:
                client.print("bounceBack");
                break;
              case 12:
                client.print("removing");
                break;
              case 13:
                client.print("wait");
                break;
              case 14:
                client.print("count down");
                break;
              case 15:
                client.print("count up");
                break;
              case 16:
                client.print("red");
                break;
              case 17:
                client.print("green");
                break;
              case 18:
                client.print("blue");
                break;
              case 19:
                client.print("flash on");
                break;
              case 20:
                client.print("mode");
                break;
            }
            client.print("</td>\n<td>");
            switch(i) {
              case 0:
                client.print("value");
                break;
              case 1:
                client.print(digitalCount);
                break;
              case 2:
                client.print(digitalPulse);
                break;
              case 3:
                client.print(digitalSearch);
                break;
              case 4:
                client.print(digitalFound);
                break;
              case 5:
                client.print(digitalShoot);
                break;
              case 6:
                client.print(digitalRed);
                break;
              case 7:
                client.print(digitalGreen);
                break;
              case 8:
                client.print(digitalBlue);
                break;
              case 9:
                client.print(brightness);
                break;
              case 10:
                client.print(wipe);
                break;
              case 11:
                client.print(bounceBack);
                break;
              case 12:
                client.print(removing);
                break;
              case 13:
                client.print(wait);
                break;
              case 14:
                client.print(countDown);
                break;
              case 15:
                client.print(countUp);
                break;
              case 16:
                client.print(red);
                break;
              case 17:
                client.print(green);
                break;
              case 18:
                client.print(blue);
                break;
              case 19:
                client.print(flashOn);
                break;
              case 20:
                client.print(startMode);
                break;
            }
            client.println("</td>\n</tr>");
          }
          client.println("</table>");
          client.println("</html>");
          break;
        }
        if (c == '\n') {
          // you're starting a new line
          currentLineIsBlank = true;
        } 
        else if (c != '\r') {
          // you've gotten a character on the current line
          currentLineIsBlank = false;
        }
      }
    }
    // give the web browser time to receive the data
    delay(1);
    // close the connection:
    client.stop();
    Serial.println("client disconnected");
  }
}
