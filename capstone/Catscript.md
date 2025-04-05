# Introduction

Catscript is a simple, and easy to learn high level statically typed programming language. Catscript is made to be readable and has a small type system. Catscript compiles and run in Java Virtual Machine.

# Features

Arithmetic Operators  
Plus symbol is used for the Addition operator `+`  
Minus symbol is used Substraction operator `-`  
Astrik symbol is used for Multiplication operator `*`  
Forward Slash is used for the Division operator `/`
```
print(10+10)
print(10-10)
print(10*10)
print(10/10)
```
Output: 
```
20
0
100
1
```


## Type System

* int - a 32 bit integer
* string - a java-style string
* bool - a boolean value
* list<x> - a list of value with the type 'x'
* null - the null type
* object - any type of value

## Variables
Declaring and assigning a string variable, to specify that it is a string type add `:` after the variable name. As follows:
```javascript
var x : string = "Hello, Cats!"
var x = "Hello, Cats!" // this should also work
```

Declaring and assigning an integer variable as follows:
```javascript
var x : int = 1
var x = 1 // this also works.
```

## Lists
Declaring and assigning lists.

Assigning a list without declaring a type. The type of the list would default to the type of its content. For example, a list containing only integers would be an integer list. If there are different types the list would default to an object type list.
```javascript
var intLst = [1,2,3] // list of integers
var lst = [1,"apple", 3] // list of objects
```

**Integer lists**
```javascript
var lst : list<int> = [1,2,3] // identical to the one above 
```

String lists
```javascript
var stringLst : list<string> = ["apple", "orange", "huckleberry"]

```

Object list
```javascript
var objLst : list<object> = ["apple", true, 1, "object"]
```

## For loops

```javascript
for(x in [1,2,3,4]) {
  print(x)
}
```
the for loop should output
```
1 2 3 4
```

## Decision Making / Comparison

Catscripts supports Equality expressions, Comparison expressions

Equality operators:  
Equal `==` and Not Equal `!=`
```javascript
var myBool1 : bool = (10==10)
var myBool2 : bool = (10!=10)
print(myBool1)
print(myBool2)
```

Outputs:
```javascript
true
false
```
Comparison operators:  
Greater than `>`, Greater than or Equal `>=`, Less than `<`, and Less than or Equal `<=`

```javascript
var myBool0 : bool = (10>9)
print(myBool0)
var myBool1 : bool = (10>10)
print(myBool0)
var myBool2 : bool = (10>=10)

var myBool3 : bool = (9<10)
print(myBool0)
var myBool4 : bool = (10<10)
print(myBool0)
var myBool5 : bool = (10<=10)
print(myBool0)
```
Output:

```
true
false
true

true
false
true
```
### If statements

```javascript
var x : int = 42
var y : int = 42
if(x == y){
    print("x and y are the same")
} else if(x != y) {
    print("x and y are not the same")
} else {
    print("else")
}
```
Output:
```
x and y are the same
```

## Printing

Catscript uses a simple syntax for the print function similar to python's print function

```javascript
print("Hello, Cats!")
```

You can concatenate strings with integers to output a string
```javascript
var strX : string = ("Hello, Cats! ")
var intY : int = 42

print(strX + intY)
```
this would output:
```
Hello, Cats! 42
```

Printing boolean expressions:
```javascript
print(10==10)
print(20>20)
print()
```
This would output:
```
true
false
```

## Functions
There are two ways to declare a function definition with parameters

First method without specifying types of parameters:  
*Not Recommended: it could make the user input incorrect values
```javascript
var x = "Hello, Cats!"

function foo(str) {
    print(str)
}
```

Second Method declaring the types of the parameters:  
*Recommended : It is easier to read and understand the types of the parameter.
```javascript
var x = "Hello, Cats!"

function foo(str : string) {
    print(str)
}
```

Function call:
```javascript
foo(x)
```

Outputs:

```
Hello, Cats!
```

Function without any parameters:

```javascript
function foo(){
    print("A function without parameters")
}

foo()
```

Output:

```
A function without parameters
```

## Comments
Catscript supports comments. To comment a line or write a comment use `//` before the line or start typing.

```
// Catscript Comment
```