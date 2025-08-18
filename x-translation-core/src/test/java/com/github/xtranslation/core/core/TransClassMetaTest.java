package com.github.xtranslation.core.core;

import com.github.xtranslation.core.dto.UserDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class TransClassMetaTest {

    @Test
    void getTransKeyField() {
        TransClassMeta transClassMeta = new TransClassMeta(UserDto.class);
        Assertions.assertTrue(transClassMeta.needTrans());
        List<TransFieldMeta> transFieldMeta = transClassMeta.getTransFieldList();
        Assertions.assertEquals(4, transFieldMeta.size());
    }


}
