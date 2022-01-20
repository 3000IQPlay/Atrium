package net.jodah.typetools;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class ReifiedParameterizedType
implements ParameterizedType {
    private final ParameterizedType original;
    private final Type[] reifiedTypeArguments;
    private final boolean[] loop;
    private int reified = 0;

    ReifiedParameterizedType(ParameterizedType original) {
        this.original = original;
        this.reifiedTypeArguments = new Type[original.getActualTypeArguments().length];
        this.loop = new boolean[original.getActualTypeArguments().length];
    }

    void addReifiedTypeArgument(Type type) {
        if (this.reified >= this.reifiedTypeArguments.length) {
            return;
        }
        if (type == this) {
            this.loop[this.reified] = true;
        }
        this.reifiedTypeArguments[this.reified++] = type;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return this.reifiedTypeArguments;
    }

    @Override
    public Type getRawType() {
        return this.original.getRawType();
    }

    @Override
    public Type getOwnerType() {
        return this.original.getOwnerType();
    }

    public String toString() {
        Type ownerType = this.getOwnerType();
        Type rawType = this.getRawType();
        Type[] actualTypeArguments = this.getActualTypeArguments();
        StringBuilder sb = new StringBuilder();
        if (ownerType != null) {
            if (ownerType instanceof Class) {
                sb.append(((Class)ownerType).getName());
            } else {
                sb.append(ownerType.toString());
            }
            sb.append("$");
            if (ownerType instanceof ParameterizedType) {
                sb.append(rawType.getTypeName().replace(((ParameterizedType)ownerType).getRawType().getTypeName() + "$", ""));
            } else if (rawType instanceof Class) {
                sb.append(((Class)rawType).getSimpleName());
            } else {
                sb.append(rawType.getTypeName());
            }
        } else {
            sb.append(rawType.getTypeName());
        }
        if (actualTypeArguments != null && actualTypeArguments.length > 0) {
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.length; ++i) {
                if (i != 0) {
                    sb.append(", ");
                }
                Type t = actualTypeArguments[i];
                if (i >= this.reified) {
                    sb.append("?");
                    continue;
                }
                if (t == null) {
                    sb.append("null");
                    continue;
                }
                if (this.loop[i]) {
                    sb.append("...");
                    continue;
                }
                sb.append(t.getTypeName());
            }
            sb.append(">");
        }
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ReifiedParameterizedType that = (ReifiedParameterizedType)o;
        if (!this.original.equals(that.original)) {
            return false;
        }
        if (this.reifiedTypeArguments.length != that.reifiedTypeArguments.length) {
            return false;
        }
        for (int i = 0; i < this.reifiedTypeArguments.length; ++i) {
            if (this.loop[i] != that.loop[i]) {
                return false;
            }
            if (this.loop[i] || this.reifiedTypeArguments[i] == that.reifiedTypeArguments[i]) continue;
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = this.original.hashCode();
        for (int i = 0; i < this.reifiedTypeArguments.length; ++i) {
            if (this.loop[i] || !(this.reifiedTypeArguments[i] instanceof ReifiedParameterizedType)) continue;
            result = 31 * result + this.reifiedTypeArguments[i].hashCode();
        }
        return result;
    }
}

