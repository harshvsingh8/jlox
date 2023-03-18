

// class Egotist {
//     speak() {
// 	print this;
//     }
// }

// var m = Egotist().speak;
// m();


// class Cake {
//     taste() {
// 	var adjective = "delicious";
// 	print "The " + this.flavor + " cake is " + adjective + "!";
//     }
// }

// var cake = Cake();
// cake.flavor = "chocolate";
// cake.taste();

class Thing {

    init(name) {
	print "initializing Thing for:" + name;
	this.n = name;
    }
    
    getCallback() {
	fun localFunction() {
	    print this;
	}
	return localFunction;
    }

    sayHi(name) {
	print "Hi from: " + this.n + " to " + name;
    }
}

// var callback = Thing().getCallback();
// callback();

    
// fun notAllowed() {
//     print this;
// }

var thingy = Thing("Harsh");
thingy.sayHi("Vardhan");
// var thingy2 = thingy.init();

// TODO - the following callback cannot be directly called like thingy2.getCallback()();
// var cb = thingy2.getCallback();
// cb();
