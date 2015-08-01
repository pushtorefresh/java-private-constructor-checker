package com.pushtorefresh.private_constructor_checker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class PrivateConstructorChecker<T> {

    private final Class<T> clazz;

    private final Class<? extends Throwable> expectedTypeOfException;

    private final String expectedExceptionMessage;

    private PrivateConstructorChecker(Class<T> clazz,
                                      Class<? extends Throwable> expectedTypeOfException,
                                      String expectedExceptionMessage) {
        this.clazz = clazz;
        this.expectedTypeOfException = expectedTypeOfException;
        this.expectedExceptionMessage = expectedExceptionMessage;
    }

    public static class Builder<T> {

        private final Class<T> clazz;

        private Class<? extends Throwable> expectedTypeOfException;

        private String expectedExceptionMessage;

        Builder(Class<T> clazz) {
            this.clazz = clazz;
        }

        public Builder<T> expectedTypeOfException(Class<? extends Throwable> expectedTypeOfException) {
            this.expectedTypeOfException = expectedTypeOfException;
            return this;
        }

        public Builder<T> expectedExceptionMessage(String expectedExceptionMessage) {
            this.expectedExceptionMessage = expectedExceptionMessage;
            return this;
        }

        public void check() {
            new PrivateConstructorChecker<T>(
                    clazz,
                    expectedTypeOfException,
                    expectedExceptionMessage
            ).check();
        }
    }

    public static <T> Builder<T> forClass(Class<T> clazz) {
        return new Builder<T>(clazz);
    }

    public void check() {
        final Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if (constructors.length > 1) {
            throw new AssertionError("Class has more than one constructor");
        }

        final Constructor<?> constructor = constructors[0];

        if (constructor.getParameterTypes().length > 0) {
            throw new AssertionError("Class has non-default constructor with some parameters");
        }

        constructor.setAccessible(true);

        if (!Modifier.isPrivate(constructor.getModifiers())) {
            throw new AssertionError("Constructor must be private");
        }

        try {
            constructor.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Can not instantiate instance of " + clazz, e);
        } catch (IllegalAccessException e) {
            // Fixed by setAccessible(true)
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();

            // It's okay case if we expect some exception from this constructor
            if (expectedTypeOfException != null || expectedExceptionMessage != null) {
                if (expectedTypeOfException != null && !expectedTypeOfException.equals(cause.getClass())) {
                    throw new IllegalStateException("Expected exception of type = "
                            + expectedTypeOfException + ", but was exception of type = "
                            + e.getCause().getClass()
                    );
                }

                if (expectedExceptionMessage != null && !expectedExceptionMessage.equals(cause.getMessage())) {
                    throw new IllegalStateException("Expected exception message = '"
                            + expectedExceptionMessage + "', but was = '"
                            + cause.getMessage() + "'",
                            e.getCause()
                    );
                }

                // Everything is okay
            } else {
                throw new IllegalStateException("No exception was expected", e);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Looks like constructor of " + clazz + " is not default", e);
        }
    }
}
