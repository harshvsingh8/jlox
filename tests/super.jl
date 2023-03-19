
class  Foo {
    sayHi() {
        return "hi";
    }      
}

class Bar : Foo {
}

var b = Bar();

print b.sayHi();


class A {
    method() {
	print "A method";
    }
}

class B : A {
    method() {
	print "B method";
    }
    test() {
	super.method();
    }
}

class C : B {
}

var c = C();
c.test();

class BadClass {

bad() {
    super.Hi();
}

}
