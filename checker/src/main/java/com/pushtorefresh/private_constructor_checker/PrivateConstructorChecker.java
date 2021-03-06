package com.pushtorefresh.private_constructor_checker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class PrivateConstructorChecker {

    private final List<Class> classes;

    private final Class<? extends Throwable> expectedTypeOfException;

    private final String expectedExceptionMessage;

    private Class[] expectedParameters;

    private PrivateConstructorChecker(List<Class> classes,
                                      Class<? extends Throwable> expectedTypeOfException,
                                      String expectedExceptionMessage,
                                      Class[] expectedParameters) {
        this.classes = classes;
        this.expectedTypeOfException = expectedTypeOfException;
        this.expectedExceptionMessage = expectedExceptionMessage;
        this.expectedParameters = expectedParameters;
    }

    public interface Checkable {
        /**
         * Runs the check which will assert that required class has one private constructor
         * which throws or not throws exception and may or may not have expected parameters.
         */
        void check();
    }

    public interface ExceptionCheckable extends Checkable {
        /**
         * Sets the expected message of the exception that must be thrown by the constructor of required class.
         * <p>
         * If you don't want to check the type of the exception, you can set just a message.
         *
         * @param expectedExceptionMessage message of the exception that must be thrown by the constructor
         *                                 of required class, should not be {@code null}.
         * @return ExceptionCheckAble.
         */
        ExceptionCheckable expectedExceptionMessage(String expectedExceptionMessage);

        /**
         * Sets the expected type of exception that must be thrown by the constructor of required class.
         * <p>
         * If you don't want to check exception message, you can set just type of the exception.
         *
         * @param expectedTypeOfException type of the exception that must be thrown by the constructor
         *                                of required class, should not be {@code null}.
         * @return ExceptionCheckAble.
         */
        ExceptionCheckable expectedTypeOfException(Class<? extends Throwable> expectedTypeOfException);
    }

    public interface ParametersCheckable extends Checkable {
        /**
         * Sets the expected parameters that must match the constructor parameters of required class.
         *
         * @param parameters type of parameters that must match the constructor parameters
         *                   of required class, should not be {@code null}.
         * @return ExceptionCheckAble.
         */
        ParametersCheckable expectedWithParameters(final Class<?>... parameters);
    }

    public static class Builder implements ExceptionCheckable, ParametersCheckable {

        private final List<Class> classes;

        private Class<? extends Throwable> expectedTypeOfException;

        private String expectedExceptionMessage;

        private Class[] expectedParameters;

        Builder(List<Class> classes) {
            this.classes = classes;
        }

        @Override
        public ExceptionCheckable expectedTypeOfException(Class<? extends Throwable> expectedTypeOfException) {
            if (expectedTypeOfException == null) {
                throw new IllegalArgumentException("expectedTypeOfException can not be null");
            }

            this.expectedTypeOfException = expectedTypeOfException;
            return this;
        }

        @Override
        public ExceptionCheckable expectedExceptionMessage(String expectedExceptionMessage) {
            if (expectedExceptionMessage == null) {
                throw new IllegalArgumentException("expectedExceptionMessage can not be null");
            }

            this.expectedExceptionMessage = expectedExceptionMessage;
            return this;
        }

        @Override
        public ParametersCheckable expectedWithParameters(final Class<?>... expectedParameters) {
            if (expectedParameters == null) {
                throw new IllegalArgumentException("expectedParameters can not be null");
            }

            this.expectedParameters = expectedParameters;
            return this;
        }

        @Override
        public void check() {
            new PrivateConstructorChecker(
                    classes,
                    expectedTypeOfException,
                    expectedExceptionMessage,
                    expectedParameters
            ).check();
        }
    }

    /**
     * Creates instance of {@link Builder}.
     *
     * @param clazz class that needs to be checked.
     * @return {@link Builder} which will prepare
     *         check of the passed class.
     */
    public static Builder forClass(Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        return new Builder(Collections.singletonList(clazz));
    }

    public static Builder forClasses(Class... classes) {
        if (classes == null || classes.length == 0) {
            throw new IllegalArgumentException("classes can not be null or empty");
        }
        for (Class clazz : classes) {
            if (clazz == null) {
                throw new IllegalArgumentException("class can not be null");
            }
        }

        return new Builder(Collections.unmodifiableList(asList(classes)));
    }

    /**
     * Runs the check which will assert that all required classes have one private constructor
     * which throws or not throws exception.
     */
    public void check() {
        for (Class clazz : classes) {
            check(clazz);
        }
    }

    /**
     * Runs the check which will assert that particular class has one private constructor
     * which throws or not throws exception.
     *
     * @param clazz class that needs to be checked.
     */
    private void check(Class clazz) {

        final Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if (constructors.length > 1) {
            throw new AssertionError(clazz + " has more than one constructor");
        }

        final Constructor<?> constructor = constructors[0];

        constructor.setAccessible(true);

        if (!Modifier.isPrivate(constructor.getModifiers())) {
            throw new AssertionError("Constructor of " + clazz + " must be private");
        }

        final Class<?>[] parameterTypes = constructor.getParameterTypes();

        if (parameterTypes.length > 0) {
            if (expectedParameters == null) {
                throw new AssertionError(clazz + " has non-default constructor with some parameters");
            } else {
                if (!Arrays.equals(parameterTypes, expectedParameters)) {
                    throw new AssertionError("Expected constructor with parameters " + getReadableClassesOutput(expectedParameters) + " but found constructor with parameters " + getReadableClassesOutput(parameterTypes));
                } else {
                    return;
                }
            }
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
                    throw new IllegalStateException("For " + clazz + " expected exception of type = " + expectedTypeOfException + ", but was exception of type = " + e.getCause().getClass());
                }

                if (expectedExceptionMessage != null && !expectedExceptionMessage.equals(cause.getMessage())) {
                    throw new IllegalStateException("For " + clazz + " expected exception message = '" + expectedExceptionMessage + "', but was = '" + cause.getMessage() + "'", e.getCause());
                }

                // Everything is okay
            } else {
                throw new IllegalStateException("For " + clazz + " no exception was expected", e);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Looks like constructor of " + clazz + " is not default", e);
        }
    }

    private static String getReadableClassesOutput(final Class<?>[] classes) {
        final StringBuilder stringBuilder = new StringBuilder("(");

        if (classes.length > 0) {
            final String concatenator = ", ";

            for (final Class<?> clazz : classes) {
                stringBuilder.append(clazz.getSimpleName()).append(concatenator);
            }

            stringBuilder.setLength(stringBuilder.length() - concatenator.length());
        }

        return stringBuilder.append(')').toString();
    }
}
