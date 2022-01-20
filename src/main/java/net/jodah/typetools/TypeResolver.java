package net.jodah.typetools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.jodah.typetools.ReifiedParameterizedType;
import sun.misc.Unsafe;

public final class TypeResolver {
    private static final Map<Class<?>, Reference<Map<TypeVariable<?>, Type>>> TYPE_VARIABLE_CACHE = Collections.synchronizedMap(new WeakHashMap());
    private static volatile boolean CACHE_ENABLED = true;
    private static boolean RESOLVES_LAMBDAS;
    private static Object JAVA_LANG_ACCESS;
    private static Method GET_CONSTANT_POOL;
    private static Method GET_CONSTANT_POOL_SIZE;
    private static Method GET_CONSTANT_POOL_METHOD_AT;
    private static final Map<String, Method> OBJECT_METHODS;
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS;
    private static final Double JAVA_VERSION;

    private TypeResolver() {
    }

    public static void enableCache() {
        CACHE_ENABLED = true;
    }

    public static void disableCache() {
        TYPE_VARIABLE_CACHE.clear();
        CACHE_ENABLED = false;
    }

    public static <T, S extends T> Class<?> resolveRawArgument(Class<T> type, Class<S> subType) {
        return TypeResolver.resolveRawArgument(TypeResolver.resolveGenericType(type, subType), subType);
    }

    public static Class<?> resolveRawArgument(Type genericType, Class<?> subType) {
        Class<?>[] arguments = TypeResolver.resolveRawArguments(genericType, subType);
        if (arguments == null) {
            return Unknown.class;
        }
        if (arguments.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument for generic type " + genericType + " but found " + arguments.length);
        }
        return arguments[0];
    }

    public static <T, S extends T> Class<?>[] resolveRawArguments(Class<T> type, Class<S> subType) {
        return TypeResolver.resolveRawArguments(TypeResolver.resolveGenericType(type, subType), subType);
    }

    public static <T, S extends T> Type reify(Class<T> type, Class<S> context) {
        return TypeResolver.reify(TypeResolver.resolveGenericType(type, context), TypeResolver.getTypeVariableMap(context, null));
    }

    public static Type reify(Type type, Class<?> context) {
        return TypeResolver.reify(type, TypeResolver.getTypeVariableMap(context, null));
    }

    public static Type reify(Type type) {
        return TypeResolver.reify(type, new HashMap(0));
    }

    public static Class<?>[] resolveRawArguments(Type genericType, Class<?> subType) {
        Class[] result;
        block5: {
            Class functionalInterface;
            block6: {
                block4: {
                    result = null;
                    functionalInterface = null;
                    if (RESOLVES_LAMBDAS && subType.isSynthetic()) {
                        Class fi;
                        Class class_ = genericType instanceof ParameterizedType && ((ParameterizedType)genericType).getRawType() instanceof Class ? (Class)((ParameterizedType)genericType).getRawType() : (fi = genericType instanceof Class ? (Class)genericType : null);
                        if (fi != null && fi.isInterface()) {
                            functionalInterface = fi;
                        }
                    }
                    if (!(genericType instanceof ParameterizedType)) break block4;
                    ParameterizedType paramType = (ParameterizedType)genericType;
                    Type[] arguments = paramType.getActualTypeArguments();
                    result = new Class[arguments.length];
                    for (int i = 0; i < arguments.length; ++i) {
                        result[i] = TypeResolver.resolveRawClass(arguments[i], subType, functionalInterface);
                    }
                    break block5;
                }
                if (!(genericType instanceof TypeVariable)) break block6;
                result = new Class[]{TypeResolver.resolveRawClass(genericType, subType, functionalInterface)};
                break block5;
            }
            if (!(genericType instanceof Class)) break block5;
            TypeVariable<Class<T>>[] typeParams = ((Class)genericType).getTypeParameters();
            result = new Class[typeParams.length];
            for (int i = 0; i < typeParams.length; ++i) {
                result[i] = TypeResolver.resolveRawClass(typeParams[i], subType, functionalInterface);
            }
        }
        return result;
    }

    public static Type resolveGenericType(Class<?> type, Type subType) {
        Type superClass;
        Type result;
        Class rawType = subType instanceof ParameterizedType ? (Class)((ParameterizedType)subType).getRawType() : (Class)subType;
        if (type.equals(rawType)) {
            return subType;
        }
        if (type.isInterface()) {
            for (Type superInterface : rawType.getGenericInterfaces()) {
                if (superInterface == null || superInterface.equals(Object.class) || (result = TypeResolver.resolveGenericType(type, superInterface)) == null) continue;
                return result;
            }
        }
        if ((superClass = rawType.getGenericSuperclass()) != null && !superClass.equals(Object.class) && (result = TypeResolver.resolveGenericType(type, superClass)) != null) {
            return result;
        }
        return null;
    }

    public static Class<?> resolveRawClass(Type genericType, Class<?> subType) {
        return TypeResolver.resolveRawClass(genericType, subType, null);
    }

    private static Class<?> resolveRawClass(Type genericType, Class<?> subType, Class<?> functionalInterface) {
        if (genericType instanceof Class) {
            return (Class)genericType;
        }
        if (genericType instanceof ParameterizedType) {
            return TypeResolver.resolveRawClass(((ParameterizedType)genericType).getRawType(), subType, functionalInterface);
        }
        if (genericType instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType)genericType;
            Class<?> component = TypeResolver.resolveRawClass(arrayType.getGenericComponentType(), subType, functionalInterface);
            return Array.newInstance(component, 0).getClass();
        }
        if (genericType instanceof TypeVariable) {
            TypeVariable variable = (TypeVariable)genericType;
            genericType = TypeResolver.getTypeVariableMap(subType, functionalInterface).get(variable);
            genericType = genericType == null ? TypeResolver.resolveBound(variable) : TypeResolver.resolveRawClass(genericType, subType, functionalInterface);
        }
        return genericType instanceof Class ? (Class)genericType : Unknown.class;
    }

    private static Type reify(Type genericType, Map<TypeVariable<?>, Type> typeVariableTypeMap) {
        if (genericType == null) {
            return null;
        }
        if (genericType instanceof Class) {
            return genericType;
        }
        return TypeResolver.reify(genericType, typeVariableTypeMap, new HashMap<ParameterizedType, ReifiedParameterizedType>());
    }

    private static Type reify(Type genericType, Map<TypeVariable<?>, Type> typeVariableMap, Map<ParameterizedType, ReifiedParameterizedType> partial) {
        if (genericType instanceof Class) {
            return genericType;
        }
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericType;
            if (partial.containsKey(parameterizedType)) {
                ReifiedParameterizedType res = partial.get(genericType);
                res.addReifiedTypeArgument(res);
                return res;
            }
            Type[] genericTypeArguments = parameterizedType.getActualTypeArguments();
            ReifiedParameterizedType result = new ReifiedParameterizedType(parameterizedType);
            partial.put(parameterizedType, result);
            for (Type genericTypeArgument : genericTypeArguments) {
                Type reified = TypeResolver.reify(genericTypeArgument, typeVariableMap, partial);
                if (reified == result) continue;
                result.addReifiedTypeArgument(reified);
            }
            return result;
        }
        if (genericType instanceof GenericArrayType) {
            Type reifiedComponentType;
            GenericArrayType genericArrayType = (GenericArrayType)genericType;
            Type genericComponentType = genericArrayType.getGenericComponentType();
            if (genericComponentType == (reifiedComponentType = TypeResolver.reify(genericArrayType.getGenericComponentType(), typeVariableMap, partial))) {
                return genericComponentType;
            }
            if (reifiedComponentType instanceof Class) {
                return Array.newInstance((Class)reifiedComponentType, 0).getClass();
            }
            throw new UnsupportedOperationException("Attempted to reify generic array type, whose generic component type could not be reified to some Class<?>. Handling for this case is not implemented");
        }
        if (genericType instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable)genericType;
            Type mapping = typeVariableMap.get(typeVariable);
            if (mapping != null) {
                return TypeResolver.reify(mapping, typeVariableMap, partial);
            }
            return TypeResolver.reify(typeVariable.getBounds()[0], typeVariableMap, partial);
        }
        if (genericType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)genericType;
            Type[] upperBounds = wildcardType.getUpperBounds();
            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (upperBounds.length == 1 && lowerBounds.length == 0) {
                return TypeResolver.reify(upperBounds[0], typeVariableMap, partial);
            }
            throw new UnsupportedOperationException("Attempted to reify wildcard type with name '" + wildcardType.getTypeName() + "' which has " + upperBounds.length + " upper bounds and " + lowerBounds.length + " lower bounds. Reification of wildcard types is only supported for the trivial case of exactly 1 upper bound and 0 lower bounds.");
        }
        throw new UnsupportedOperationException("Reification of type with name '" + genericType.getTypeName() + "' and class name '" + genericType.getClass().getName() + "' is not implemented.");
    }

    private static Map<TypeVariable<?>, Type> getTypeVariableMap(Class<?> targetType, Class<?> functionalInterface) {
        Map<TypeVariable<?>, Type> map;
        Reference<Map<TypeVariable<?>, Type>> ref = TYPE_VARIABLE_CACHE.get(targetType);
        Map<TypeVariable<?>, Type> map2 = map = ref != null ? ref.get() : null;
        if (map == null) {
            Class<?> type;
            map = new HashMap();
            if (functionalInterface != null) {
                TypeResolver.populateLambdaArgs(functionalInterface, targetType, map);
            }
            TypeResolver.populateSuperTypeArgs(targetType.getGenericInterfaces(), map, functionalInterface != null);
            Type genericType = targetType.getGenericSuperclass();
            for (type = targetType.getSuperclass(); type != null && !Object.class.equals(type); type = type.getSuperclass()) {
                if (genericType instanceof ParameterizedType) {
                    TypeResolver.populateTypeArgs((ParameterizedType)genericType, map, false);
                }
                TypeResolver.populateSuperTypeArgs(type.getGenericInterfaces(), map, false);
                genericType = type.getGenericSuperclass();
            }
            type = targetType;
            while (type.isMemberClass()) {
                genericType = type.getGenericSuperclass();
                if (genericType instanceof ParameterizedType) {
                    TypeResolver.populateTypeArgs((ParameterizedType)genericType, map, functionalInterface != null);
                }
                type = type.getEnclosingClass();
            }
            if (CACHE_ENABLED) {
                TYPE_VARIABLE_CACHE.put(targetType, new WeakReference(map));
            }
        }
        return map;
    }

    private static void populateSuperTypeArgs(Type[] types, Map<TypeVariable<?>, Type> map, boolean depthFirst) {
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                Type rawType;
                ParameterizedType parameterizedType = (ParameterizedType)type;
                if (!depthFirst) {
                    TypeResolver.populateTypeArgs(parameterizedType, map, depthFirst);
                }
                if ((rawType = parameterizedType.getRawType()) instanceof Class) {
                    TypeResolver.populateSuperTypeArgs(((Class)rawType).getGenericInterfaces(), map, depthFirst);
                }
                if (!depthFirst) continue;
                TypeResolver.populateTypeArgs(parameterizedType, map, depthFirst);
                continue;
            }
            if (!(type instanceof Class)) continue;
            TypeResolver.populateSuperTypeArgs(((Class)type).getGenericInterfaces(), map, depthFirst);
        }
    }

    private static void populateTypeArgs(ParameterizedType type, Map<TypeVariable<?>, Type> map, boolean depthFirst) {
        if (type.getRawType() instanceof Class) {
            Type owner;
            TypeVariable<Class<T>>[] typeVariables = ((Class)type.getRawType()).getTypeParameters();
            Type[] typeArguments = type.getActualTypeArguments();
            if (type.getOwnerType() != null && (owner = type.getOwnerType()) instanceof ParameterizedType) {
                TypeResolver.populateTypeArgs((ParameterizedType)owner, map, depthFirst);
            }
            for (int i = 0; i < typeArguments.length; ++i) {
                Type existingType;
                TypeVariable variable = typeVariables[i];
                Type typeArgument = typeArguments[i];
                if (typeArgument instanceof Class) {
                    map.put(variable, typeArgument);
                    continue;
                }
                if (typeArgument instanceof GenericArrayType) {
                    map.put(variable, typeArgument);
                    continue;
                }
                if (typeArgument instanceof ParameterizedType) {
                    map.put(variable, typeArgument);
                    continue;
                }
                if (!(typeArgument instanceof TypeVariable)) continue;
                TypeVariable typeVariableArgument = (TypeVariable)typeArgument;
                if (depthFirst && (existingType = map.get(variable)) != null) {
                    map.put(typeVariableArgument, existingType);
                    continue;
                }
                Type resolvedType = map.get(typeVariableArgument);
                if (resolvedType == null) {
                    resolvedType = TypeResolver.resolveBound(typeVariableArgument);
                }
                map.put(variable, resolvedType);
            }
        }
    }

    public static Type resolveBound(TypeVariable<?> typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return Unknown.class;
        }
        Type bound = bounds[0];
        if (bound instanceof TypeVariable) {
            bound = TypeResolver.resolveBound((TypeVariable)bound);
        }
        return bound == Object.class ? Unknown.class : bound;
    }

    private static void populateLambdaArgs(Class<?> functionalInterface, Class<?> lambdaType, Map<TypeVariable<?>, Type> map) {
        if (RESOLVES_LAMBDAS) {
            for (Method m : functionalInterface.getMethods()) {
                Method objectMethod;
                if (TypeResolver.isDefaultMethod(m) || Modifier.isStatic(m.getModifiers()) || m.isBridge() || (objectMethod = OBJECT_METHODS.get(m.getName())) != null && Arrays.equals(m.getTypeParameters(), objectMethod.getTypeParameters())) continue;
                Type returnTypeVar = m.getGenericReturnType();
                Type[] paramTypeVars = m.getGenericParameterTypes();
                Member member = TypeResolver.getMemberRef(lambdaType);
                if (member == null) {
                    return;
                }
                if (returnTypeVar instanceof TypeVariable) {
                    Class<Object> returnType = member instanceof Method ? ((Method)member).getReturnType() : ((Constructor)member).getDeclaringClass();
                    if (!(returnType = TypeResolver.wrapPrimitives(returnType)).equals(Void.class)) {
                        map.put((TypeVariable)returnTypeVar, returnType);
                    }
                }
                Class<?>[] arguments = member instanceof Method ? ((Method)member).getParameterTypes() : ((Constructor)member).getParameterTypes();
                int paramOffset = 0;
                if (paramTypeVars.length > 0 && paramTypeVars[0] instanceof TypeVariable && paramTypeVars.length == arguments.length + 1) {
                    Class<?> instanceType = member.getDeclaringClass();
                    map.put((TypeVariable)paramTypeVars[0], instanceType);
                    paramOffset = 1;
                }
                int argOffset = 0;
                if (paramTypeVars.length < arguments.length) {
                    argOffset = arguments.length - paramTypeVars.length;
                }
                int i = 0;
                while (i + argOffset < arguments.length) {
                    if (paramTypeVars[i] instanceof TypeVariable) {
                        map.put((TypeVariable)paramTypeVars[i + paramOffset], TypeResolver.wrapPrimitives(arguments[i + argOffset]));
                    }
                    ++i;
                }
                return;
            }
        }
    }

    private static boolean isDefaultMethod(Method m) {
        return JAVA_VERSION >= 1.8 && m.isDefault();
    }

    private static Member getMemberRef(Class<?> type) {
        Object constantPool;
        try {
            constantPool = GET_CONSTANT_POOL.invoke(JAVA_LANG_ACCESS, type);
        }
        catch (Exception ignore) {
            return null;
        }
        Member result = null;
        for (int i = TypeResolver.getConstantPoolSize(constantPool) - 1; i >= 0; --i) {
            Member member = TypeResolver.getConstantPoolMethodAt(constantPool, i);
            if (member == null || member instanceof Constructor && member.getDeclaringClass().getName().equals("java.lang.invoke.SerializedLambda") || member.getDeclaringClass().isAssignableFrom(type)) continue;
            result = member;
            if (!(member instanceof Method) || !TypeResolver.isAutoBoxingMethod((Method)member)) break;
        }
        return result;
    }

    private static boolean isAutoBoxingMethod(Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        return method.getName().equals("valueOf") && parameters.length == 1 && parameters[0].isPrimitive() && TypeResolver.wrapPrimitives(parameters[0]).equals(method.getDeclaringClass());
    }

    private static Class<?> wrapPrimitives(Class<?> clazz) {
        return clazz.isPrimitive() ? PRIMITIVE_WRAPPERS.get(clazz) : clazz;
    }

    private static int getConstantPoolSize(Object constantPool) {
        try {
            return (Integer)GET_CONSTANT_POOL_SIZE.invoke(constantPool, new Object[0]);
        }
        catch (Exception ignore) {
            return 0;
        }
    }

    private static Member getConstantPoolMethodAt(Object constantPool, int i) {
        try {
            return (Member)GET_CONSTANT_POOL_METHOD_AT.invoke(constantPool, i);
        }
        catch (Exception ignore) {
            return null;
        }
    }

    static {
        OBJECT_METHODS = new HashMap<String, Method>();
        JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));
        try {
            AccessMaker accessSetter;
            Class<?> sharedSecretsClass;
            final Unsafe unsafe = AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>(){

                @Override
                public Unsafe run() throws Exception {
                    Field f = Unsafe.class.getDeclaredField("theUnsafe");
                    f.setAccessible(true);
                    return (Unsafe)f.get(null);
                }
            });
            if (JAVA_VERSION < 9.0) {
                sharedSecretsClass = Class.forName("sun.misc.SharedSecrets");
                accessSetter = new AccessMaker(){

                    @Override
                    public void makeAccessible(AccessibleObject accessibleObject) {
                        accessibleObject.setAccessible(true);
                    }
                };
            } else if (JAVA_VERSION < 12.0) {
                try {
                    sharedSecretsClass = Class.forName("jdk.internal.misc.SharedSecrets");
                }
                catch (ClassNotFoundException e) {
                    sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
                }
                Field overrideField = AccessibleObject.class.getDeclaredField("override");
                final long overrideFieldOffset = unsafe.objectFieldOffset(overrideField);
                accessSetter = new AccessMaker(){

                    @Override
                    public void makeAccessible(AccessibleObject accessibleObject) {
                        unsafe.putBoolean(accessibleObject, overrideFieldOffset, true);
                    }
                };
            } else {
                sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
                Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                long implLookupFieldOffset = unsafe.staticFieldOffset(implLookupField);
                Object lookupStaticFieldBase = unsafe.staticFieldBase(implLookupField);
                MethodHandles.Lookup implLookup = (MethodHandles.Lookup)unsafe.getObject(lookupStaticFieldBase, implLookupFieldOffset);
                final MethodHandle overrideSetter = implLookup.findSetter(AccessibleObject.class, "override", Boolean.TYPE);
                accessSetter = new AccessMaker(){

                    @Override
                    public void makeAccessible(AccessibleObject object) throws Throwable {
                        overrideSetter.invokeWithArguments(object, true);
                    }
                };
            }
            Method javaLangAccessGetter = sharedSecretsClass.getMethod("getJavaLangAccess", new Class[0]);
            accessSetter.makeAccessible(javaLangAccessGetter);
            JAVA_LANG_ACCESS = javaLangAccessGetter.invoke(null, new Object[0]);
            GET_CONSTANT_POOL = JAVA_LANG_ACCESS.getClass().getMethod("getConstantPool", Class.class);
            String constantPoolName = JAVA_VERSION < 9.0 ? "sun.reflect.ConstantPool" : "jdk.internal.reflect.ConstantPool";
            Class<?> constantPoolClass = Class.forName(constantPoolName);
            GET_CONSTANT_POOL_SIZE = constantPoolClass.getDeclaredMethod("getSize", new Class[0]);
            GET_CONSTANT_POOL_METHOD_AT = constantPoolClass.getDeclaredMethod("getMethodAt", Integer.TYPE);
            accessSetter.makeAccessible(GET_CONSTANT_POOL);
            accessSetter.makeAccessible(GET_CONSTANT_POOL_SIZE);
            accessSetter.makeAccessible(GET_CONSTANT_POOL_METHOD_AT);
            Object constantPool = GET_CONSTANT_POOL.invoke(JAVA_LANG_ACCESS, Object.class);
            GET_CONSTANT_POOL_SIZE.invoke(constantPool, new Object[0]);
            for (Method method : Object.class.getDeclaredMethods()) {
                OBJECT_METHODS.put(method.getName(), method);
            }
            RESOLVES_LAMBDAS = true;
        }
        catch (Throwable unsafe) {
            // empty catch block
        }
        HashMap<Class<Object>, Class<Void>> types = new HashMap<Class<Object>, Class<Void>>();
        types.put(Boolean.TYPE, Boolean.class);
        types.put(Byte.TYPE, Byte.class);
        types.put(Character.TYPE, Character.class);
        types.put(Double.TYPE, Double.class);
        types.put(Float.TYPE, Float.class);
        types.put(Integer.TYPE, Integer.class);
        types.put(Long.TYPE, Long.class);
        types.put(Short.TYPE, Short.class);
        types.put(Void.TYPE, Void.class);
        PRIMITIVE_WRAPPERS = Collections.unmodifiableMap(types);
    }

    public static final class Unknown {
        private Unknown() {
        }
    }

    private static interface AccessMaker {
        public void makeAccessible(AccessibleObject var1) throws Throwable;
    }
}

