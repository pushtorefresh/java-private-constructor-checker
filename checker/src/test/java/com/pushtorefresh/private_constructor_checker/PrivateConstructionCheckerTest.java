package com.pushtorefresh.private_constructor_checker;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PrivateConstructionCheckerTest {
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void builderShouldThrowExceptionIfNullWasPassedAsExpectedTypeOfException() {
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClass(Object.class);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("expectedTypeOfException can not be null");
        builder.expectedTypeOfException(null);
    }

    @Test
    public void builderShouldThrowExceptionIfNullWasPassedAsExpectedExceptionMessage() {
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClass(Object.class);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("expectedExceptionMessage can not be null");
        builder.expectedExceptionMessage(null);
    }

    @Test
    public void builderShouldThrowExceptionIfNullWasPassedAsClass() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("class can not be null");
        PrivateConstructorChecker
                .forClass(null);
    }

    @Test
    public void builderShouldThrowExceptionIfNullWasPassedAsClasses() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("classes can not be null or empty");
        PrivateConstructorChecker
                .forClasses(null);
    }

    @Test
    public void builderShouldThrowExceptionIfEmptyArrayWasPassedAsClasses() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("classes can not be null or empty");
        PrivateConstructorChecker
                .forClasses(new Class[0]);
    }

    @Test
    public void builderShouldThrowExceptionIfNullWasPassedAsOneOfClasses() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("class can not be null");
        PrivateConstructorChecker
                .forClasses(ClassWithPrivateConstructor.class, null);
    }

    static class ClassWithoutDefaultConstructor {
        private ClassWithoutDefaultConstructor(String someParam) {
        }
    }

    @Test
    public void shouldThrowExceptionIfClassHasNonDefaultConstructor() {
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClass(ClassWithoutDefaultConstructor.class);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(ClassWithoutDefaultConstructor.class + " has non-default constructor with some parameters");
        builder.check();
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
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClass(ClassWithDefaultProtectedConstructor.class);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Constructor of " + ClassWithDefaultProtectedConstructor.class + " must be private");
        builder.check();
    }

    static class ClassWithProtectedConstructor {
        protected ClassWithProtectedConstructor() {
        }
    }

    @Test
    public void shouldThrowExceptionBecauseConstructorHasProtectedModifier() {
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClass(ClassWithProtectedConstructor.class);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Constructor of " + ClassWithProtectedConstructor.class + " must be private");
        builder.check();
    }

    static class ClassWithPublicConstructor {
        public ClassWithPublicConstructor() {
        }
    }

    @Test
    public void shouldThrowExceptionBecauseConstructorHasPublicModifier() {
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClass(ClassWithPublicConstructor.class);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Constructor of " + ClassWithPublicConstructor.class + " must be private");
        builder.check();
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
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClass(ClassWithConstructorThatThrowsException.class)
                .expectedTypeOfException(IllegalArgumentException.class); // Incorrect type

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("For " + ClassWithConstructorThatThrowsException.class + " expected exception of type = class java.lang.IllegalArgumentException, " +
                "but was exception of type = class java.lang.IllegalStateException");
        builder.check();
    }

    @Test
    public void shouldThrowExceptionBecauseExpectedMessageDoesNotMatch() {
        PrivateConstructorChecker.Builder builder =PrivateConstructorChecker
                .forClass(ClassWithConstructorThatThrowsException.class)
                .expectedTypeOfException(IllegalStateException.class) // Correct type
                .expectedExceptionMessage("lol, not something that you've expected?"); // Incorrect message

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("For " + ClassWithConstructorThatThrowsException.class + " expected exception message = 'lol, not something that you've expected?', " +
                "but was = 'test exception'");
        builder.check();
    }

    @Test
    public void shouldThrowExceptionBecauseConstructorThrownUnexpectedException() {
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClass(ClassWithConstructorThatThrowsException.class);

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("For " + ClassWithConstructorThatThrowsException.class + " no exception was expected");
        builder.check(); // We don't expect exception, but it will be thrown
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
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClass(ClassWith2Constructors.class);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(ClassWith2Constructors.class + " has more than one constructor");
        builder.check();
    }

    static class ClassWithoutDeclaredConstructor {

    }

    @Test
    public void shouldThrowExceptionBecauseClassDoesNotHaveDeclaredConstructors() {
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClass(ClassWithoutDeclaredConstructor.class);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Constructor of " + ClassWithoutDeclaredConstructor.class + " must be private");
        builder.check();
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
        PrivateConstructorChecker.Builder builder = PrivateConstructorChecker
                .forClasses(ClassWithPrivateConstructor.class, ClassWithoutDefaultConstructor.class);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(ClassWithoutDefaultConstructor.class + " has non-default constructor with some parameters");
        builder.check();
    }
}
