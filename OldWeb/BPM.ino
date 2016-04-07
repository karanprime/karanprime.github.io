//BEATS PER MINUTE BY KARAN SHAH

int led = 9;
int j = 0;
int sen = A0;
void setup() {}

void loop() {
  if(analogRead(sen) <= 150) {
    analogWrite(led,255);
    delay(400);
    for(int i=200;i>0;i--) {
        analogWrite(led,i);
        delay(2);
    }
    j++;
    if(j == 2) {
      delay(600); 
      j=0;
    }
  }
}
