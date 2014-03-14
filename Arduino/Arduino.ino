#include <SPI.h>
#include <Dhcp.h>
#include <Dns.h>
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
#define COLOR1 A3 //A3
#define COLOR2 A4 //A4
#define COLOR3 A5 //A5

Adafruit_NeoPixel leftStrip = Adafruit_NeoPixel(24, LEFT_STRIP_LED, NEO_GRB + NEO_KHZ800);
Adafruit_NeoPixel rightStrip = Adafruit_NeoPixel(24, RIGHT_STRIP_LED, NEO_GRB + NEO_KHZ800);
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
int digitalColor1 = LOW;
int digitalColor2 = LOW;
int digitalColor3 = LOW;
uint16_t brightness = 255;
uint16_t wipe = 0;
uint16_t bounceBack = false;
uint16_t removing = false;
uint8_t wait = 0;
int countDown = 10;
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
  pinMode(COLOR1, INPUT);
  pinMode(COLOR2, INPUT);
  pinMode(COLOR3, INPUT);
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
  digitalColor1 = digitalRead(COLOR1);
  digitalColor2 = digitalRead(COLOR2);
  digitalColor3 = digitalRead(COLOR3);
  if(wait>0) {
    --wait;
    delay(1);
  } else {
    if(digitalCount == HIGH) {
      setMode(1);
      count();
    } else {
      if(wipe==0) {
        switch(digitalColor1+digitalColor2*2+digitalColor3*4) {
          default:
            red = 0;
            green = 0;
            blue = 255;
            break;
          case 1:
            red = 255;
            green = 0;
            blue = 0;
            break;
          case 2:
            red = 0;
            green = 255;
            blue = 0;
            break;
          case 3:
            red = 255;
            green = 0;
            blue = 255;
            break;
          case 4:
            red = 255;
            green = 255;
            blue = 0;
            break;
          case 5:
            red = 0;
            green = 255;
            blue = 255;
            break;
          case 6:
            red = 255;
            green = 255;
            blue = 255;
            break;
          case 7:
            red = 0;
            green = 0;
            blue = 0;
            break;
        }
      }
      if (digitalShoot == HIGH) {
        takeShot();
      }
      if(brightness<255) {
        ++brightness;
      }
      if(digitalSearch == HIGH) {
        setMode(2);
        colorWipe(red,green,blue);
        wait = 50;
      } else if(digitalFound == HIGH) {
        setMode(3);
        flash(red,green,blue);
        wait = 250;
      } else {
        setMode(4);
        setColor(red,green,blue);
        wait = 50;
      }
    }
  }
  
  //TODO: save camera image from http://10.8.69.11/axis-cgi/jpg/image.cgi
  
  printHtml();
}

void setMode(int mode) {
  if(mode!=startMode) {
    wipe = 0;
    countDown = 10;
    countUp = true;
    flashOn = false;
    startMode = mode;
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
    case 9:
      setColor(255,255,255);
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
    case 0:
      setColor(0,0,0);
      break;
  }
}

// Fill the dots one after the other with a color
void colorWipe(uint8_t r, uint8_t g, uint8_t b) {
  setBrightness();
  left.setPixelColor(0, r, g, b);
  right.setPixelColor(0, r, g, b);
  if(wipe<leftStrip.numPixels()) {
    leftStrip.setPixelColor(wipe, r, g, b);
    rightStrip.setPixelColor(wipe, r, g, b);
    ++wipe;
  } else {
    setStripColor(0,0,0);
    wipe = 0;
  }
  showPixels();
}

//TODO: bounce back and forth on the strip
void colorBounce(uint8_t r, uint8_t g, uint8_t b) {
  setBrightness();
  left.setPixelColor(0, r, g, b);
  right.setPixelColor(0, r, g, b);
  if(wipe<leftStrip.numPixels()) {
    leftStrip.setPixelColor(wipe, r, g, b);
    rightStrip.setPixelColor(wipe, r, g, b);
    ++wipe;
  } else {
    setStripColor(0,0,0);
    wipe = 0;
  }
  showPixels();
}

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
          for(int i=0;i<21;++i) {
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
                client.print(digitalColor1);
                break;
              case 7:
                client.print(digitalColor2);
                break;
              case 8:
                client.print(digitalColor3);
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
