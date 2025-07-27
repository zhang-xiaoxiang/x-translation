package com.github.resolver;


import com.github.dto.Result;

public class ResultResolver implements TransObjResolver {
    @Override
    public boolean support(Object obj) {
        return obj instanceof Result;
    }

    @Override
    public Object resolveTransObj(Object obj) {
        return ((Result<?>) obj).getData();
    }

}
