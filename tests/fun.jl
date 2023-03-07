
fun fib(n) {
    var x = 1;
    var y = 1;
    while(n >=0) {
	var t = x;
	x = y;
	y = x + t;
	print x;
	n = n-1;
    }
}

fun fact(x) {
 if(x == 1)
      return 1;
 else
      return x * fact(x-1);
}

fun makeCounter() {
    var i = 0;
    fun count() {
	i = i + 1;
	print i;
    }
    return count;
}

var counter = makeCounter();
// counter();
// counter();

fun thrice(fn,Md) {
    for(var i =1; i <= 3; i = i + 1) {
	fn(Md(i));
    }
}

// fun printme(a) {
//     print a;
// }

thrice(
    fun (a) {
	print a;
    },
    fun (x) {
	return x*x;
    });

// thrice(printme);

