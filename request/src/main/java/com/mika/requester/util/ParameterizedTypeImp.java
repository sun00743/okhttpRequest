package com.mika.requester.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by mika on 2017/11/20
 */

public class ParameterizedTypeImp implements ParameterizedType {

    private Type raw;
    private Type cls;

    public ParameterizedTypeImp(Type raw, Type cls) {
        this.raw = raw;
        this.cls = cls;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{cls};
    }

    @Override
    public Type getRawType() {
        return raw;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

}
