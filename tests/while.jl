
var fib1 = 0;
var fib2 = 1;
var count = 0;
while( count < 20) {
       var temp = fib1;
       fib1 = fib2;
       fib2 = temp + fib2;
       print temp;
       count = count + 1;
}
