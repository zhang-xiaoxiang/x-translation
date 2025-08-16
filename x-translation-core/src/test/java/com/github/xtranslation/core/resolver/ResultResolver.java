package com.github.xtranslation.core.resolver;


import com.github.xtranslation.core.dto.Result;

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
