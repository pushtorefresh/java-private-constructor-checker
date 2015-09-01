###Goal
**Max code coverage!**

It means, that **you need to test** even private constructors of classes that must not be instantiated.

**Q**: Why would I want to test that constructor is private and that it throws exception??

**A**: Because if you not the only developer in the project — others may change this (make constructor public, etc) and you may miss this on the code review because you are human).

**A2**: Because if you want to have as max code coverage as possible — you need to test even such unreacheable code.

###Sample
Now, imagine you have such class with private constructor:
```java
class Checks {

  private Checks() {
    throw new IllegalStateException("No instances please!");
  }
  
  public static void checkNotNull(Object ref, String message) {
    if (ref == null) {
      throw new AssertionError(message);
    }
  }
  
  // other methods
}
```

###The problem

Let's try to test this constructor!

```java
@Test
public void constructorMustBePrivateAndThrowException() {
  try {
    new Checks(); // won't compile! Constructor is private!
    fail("constructor must throw exception");
  } catch (IllegalStateException expected) {
    assertEquals("No instances please!", expected.getMessage());
  }
}
```

Okay, we can not just write a test for private constructor, we need reflection!

```java
@Test
public void constructorMustBePrivateAndThrowException() {
  Constructor<?> constructor = Checks.class.getConstructor();
  constructor.setAccessible(true);
  
  try {
    constructor.newInstance();
    fail("constructor must throw exception");
  } catch (InvocationTargetException expected) {
    IllegalStateException cause = (IllegalStateException) expected.getCause();
    assertEquals("No instances please!", cause.getMessage());
  }
}
```

And that's not even full code that you'll have to write, you also need to check that class does not have other constructors and some other things. So, you really want to write this boilerplate unreadable code every time?

###Clear solution

```java
@Test
public void constructorMustBePrivateAndThrowException() {
  PrivateConstructorChecker
    .forClass(Checks.class) // Or you can use forClasses() and check multiple classes!
    .expectedTypeOfException(IllegalStateException.class)
    .expectedExceptionMessage("No instances please!")
    .check();
}
```

This is how we do! Clear, nice and readable!

What `PrivateConstructorChecker` does:

* Checks that class has only one constructor without args and that it's private.
* Checks that constructor throws exception (optional).
* Can check exception type and/or exception message.
* Saves you from boilerplate code!

###Download

**Gradle**:
```groovy
testCompile 'com.pushtorefresh.java-private-constructor-checker:checker:1.1.0'
```

**Maven**:
```xml
<dependency>
    <groupId>com.pushtorefresh.java-private-constructor-checker</groupId>
    <artifactId>checker</artifactId>
    <version>1.1.0</version>
</dependency>
```

Use, share, have fun!

**Made with love** in [Pushtorefresh.com](https://pushtorefresh.com) by [@artem_zin](https://twitter.com/artem_zin) and [@nikitin-da](https://github.com/nikitin-da)

