var global = "outside";
{
    var local = "inside";
    print global + local;
}

var x = 1;

{
	var x = 2;
	print x;
}

print x;


