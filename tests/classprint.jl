
class MyClass {
    MyFun(name) {
      	print "hello" + name;
    }
}

print MyClass;

var mc = MyClass();

print mc;

mc.x = "Hello";
mc.y = "World";
mc.z = mc.x + " " + mc.y;
print mc.z;


class Paratha {
    eat(with) {
      	print "Each paratha charap charap chew with " + with;
    }
}

var p = Paratha();
p.eat("aachar");
Paratha().eat("chatney");
