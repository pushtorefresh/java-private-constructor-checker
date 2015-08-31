package com.pushtorefresh.private_constructor_checker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PrivateConstructionCheckerTest {

    @Test
    public void builderShouldThrowExceptionIfNullWasPassedAsExpectedTypeOfException() {
        try {
            PrivateConstructorChecker
                    .forClass(Object.class)
                    .expectedTypeOfException(null);

            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("expectedTypeOfException can not be null", expected.getMessage());
        }
    }

    @Test
    public void builderShouldThrowExceptionIfNullWasPassedAsExpectedExceptionMessage() {
        try {
            PrivateConstructorChecker
                    .forClass(Object.class)
                    .expectedExceptionMessage(null);

            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("expectedExceptionMessage can not be null", expected.getMessage());
        }
    }

    @Test
    public void builderShouldThrowExceptionIfNullWasPassedAsClass() {
        try {
            PrivateConstructorChecker
                    .forClass(null)
                    .check();

            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("class can not be null", expected.getMessage());
        }
    }

    @Test
    public void builderShouldThrowExceptionIfNullWasPassedAsClasses() {
        try {
            PrivateConstructorChecker
                    .forClasses(null)
                    .check();

            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("classes can not be null or empty", expected.getMessage());
        }
    }

    @Test
    public void builderShouldThrowExceptionIfNullWasPassedAsOneOfClasses() {
        try {
            PrivateConstructorChecker
                    .forClasses(ClassWithPrivateConstructor.class, null)
                    .check();

            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("class can not be null", expected.getMessage());
        }
    }

    static class ClassWithoutDefaultConstructor {
        private ClassWithoutDefaultConstructor(String someParam) {
        }
    }

    @Test
    public void shouldThrowExceptionIfClassHasNonDefaultConstructor() {
        try {
            PrivateConstructorChecker
                    .forClass(ClassWithoutDefaultConstructor.class)
                    .check();

            fail();
        } catch (AssertionError expected) {
            assertEquals(
                    ClassWithoutDefaultConstructor.class + " has non-default constructor with some parameters",
                    expected.getMessage()
            );
        }
    }

    static class ClassWithPrivateConstructor {
        private ClassWithPrivateConstructor() {
        }
    }

    @Test
    public void shouldAssertThatConstructorIsPrivateAndDoesNotThrowExceptions() {
        PrivateConstructorChecker
                .forClass(ClassWithPrivateConstructor.class)
                .check();
    }

    static class ClassWithDefaultProtectedConstructor {
        ClassWithDefaultProtectedConstructor() {
        }
    }

    @Test
    public void shouldThrowExceptionBecauseConstructorHasDefaultModifier() {
        try {
            PrivateConstructorChecker
                    .forClass(ClassWithDefaultProtectedConstructor.class)
                    .check();

            fail();
        } catch (AssertionError expected) {
            assertEquals("Constructor of " + ClassWithDefaultProtectedConstructor.class + " must be private", expected.getMessage());
        }
    }

    static class ClassWithProtectedConstructor {
        protected ClassWithProtectedConstructor() {
        }
    }

    @Test
    public void shouldThrowExceptionBecauseConstructorHasProtectedModifier() {
        try {
            PrivateConstructorChecker
                    .forClass(ClassWithProtectedConstructor.class)
                    .check();

            fail();
        } catch (AssertionError expected) {
            assertEquals("Constructor of " + ClassWithProtectedConstructor.class + " must be private", expected.getMessage());
        }
    }

    static class ClassWithPublicConstructor {
        public ClassWithPublicConstructor() {
        }
    }

    @Test
    public void shouldThrowExceptionBecauseConstructorHasPublicModifier() {
        try {
            PrivateConstructorChecker
                    .forClass(ClassWithPublicConstructor.class)
                    .check();

            fail();
        } catch (AssertionError expected) {
            assertEquals("Constructor of " + ClassWithPublicConstructor.class + " must be private", expected.getMessage());
        }
    }

    static class ClassWithConstructorThatThrowsException {
        private ClassWithConstructorThatThrowsException() {
            throw new IllegalStateException("test exception");
        }
    }

    @Test
    public void shouldCheckThatConstructorThrowsExceptionWithoutCheckingMessage() {
        PrivateConstructorChecker
                .forClass(ClassWithConstructorThatThrowsException.class)
                .expectedTypeOfException(IllegalStateException.class)
                .check();
    }

    @Test
    public void shouldCheckThatConstructorThrowsExceptionWithExpectedMessage() {
        PrivateConstructorChecker
                .forClass(ClassWithConstructorThatThrowsException.class)
                .expectedTypeOfException(IllegalStateException.class)
                .expectedExceptionMessage("test exception")
                .check();
    }

    @Test
    public void shouldCheckThatConstructorThrowsExceptionWithExpectedMessageButWithoutExpectedExceptionType() {
        PrivateConstructorChecker
                .forClass(ClassWithConstructorThatThrowsException.class)
                .expectedExceptionMessage("test exception")
                .check(); // without checking exception's type
    }

    @Test
    public void shouldThrowExceptionBecauseTypeOfExpectedExceptionDoesNotMatch() {
        try {
            PrivateConstructorChecker
                    .forClass(ClassWithConstructorThatThrowsException.class)
                    .expectedTypeOfException(IllegalArgumentException.class) // Incorrect type
                    .check();

            fail();
        } catch (IllegalStateException expected) {
            assertEquals("For " + ClassWithConstructorThatThrowsException.class + " expected exception of type = class java.lang.IllegalArgumentException, " +
                            "but was exception of type = class java.lang.IllegalStateException",
                    expected.getMessage()
            );
        }
    }

    @Test
    public void shouldThrowExceptionBecauseExpectedMessageDoesNotMatch() {
        try {
            PrivateConstructorChecker
                    .forClass(ClassWithConstructorThatThrowsException.class)
                    .expectedTypeOfException(IllegalStateException.class) // Correct type
                    .expectedExceptionMessage("lol, not something that you've expected?") // Incorrect message
                    .check();

            fail();
        } catch (IllegalStateException expected) {
            assertEquals("For " + ClassWithConstructorThatThrowsException.class + " expected exception message = 'lol, not something that you've expected?', " +
                            "but was = 'test exception'",
                    expected.getMessage()
            );
        }
    }

    @Test
    public void shouldThrowExceptionBecauseConstructorThrownUnexpectedException() {
        try {
            PrivateConstructorChecker
                    .forClass(ClassWithConstructorThatThrowsException.class)
                    .check(); // We don't expect exception, but it will be thrown

            fail();
        } catch (IllegalStateException expected) {
            assertEquals("For " + ClassWithConstructorThatThrowsException.class + " no exception was expected", expected.getMessage());
        }
    }

    static class ClassWith2Constructors {
        // This is good constructor for the checker
        private ClassWith2Constructors() {

        }

        // This is bad constructor
        private ClassWith2Constructors(String str) {

        }
    }

    @Test
    public void shouldThrowExceptionBecauseClassHasMoreThanOneConstructor() {
        try {
            PrivateConstructorChecker
                    .forClass(ClassWith2Constructors.class)
                    .check();

            fail();
        } catch (AssertionError expected) {
            assertEquals(ClassWith2Constructors.class + " has more than one constructor", expected.getMessage());
        }
    }

    static class ClassWithoutDeclaredConstructor {

    }

    @Test
    public void shouldThrowExceptionBecauseClassDoesNotHaveDeclaredConstructors() {
        try {
            PrivateConstructorChecker
                    .forClass(ClassWithoutDeclaredConstructor.class)
                    .check();

            fail();
        } catch (AssertionError expected) {
            assertEquals("Constructor of " + ClassWithoutDeclaredConstructor.class + " must be private", expected.getMessage());
        }
    }

    static class AnotherClassWithConstructorThatThrowsException {
        private AnotherClassWithConstructorThatThrowsException() {
            throw new IllegalStateException("test exception");
        }
    }

    @Test
    public void shouldCheckMultipleThatConstructorThrowsExceptionWithExpectedMessage() {
        PrivateConstructorChecker
                .forClasses(ClassWithConstructorThatThrowsException.class, AnotherClassWithConstructorThatThrowsException.class)
                .expectedTypeOfException(IllegalStateException.class)
                .expectedExceptionMessage("test exception")
                .check();
    }

    @Test
    public void shouldThrowExceptionIfOneOfClassesHasNonDefaultConstructor() {
        try {
            PrivateConstructorChecker
                    .forClasses(ClassWithPrivateConstructor.class, ClassWithoutDefaultConstructor.class)
                    .check();

            fail();
        } catch (AssertionError expected) {
            assertEquals(
                    ClassWithoutDefaultConstructor.class + " has non-default constructor with some parameters",
                    expected.getMessage()
            );
        }
    }
}
