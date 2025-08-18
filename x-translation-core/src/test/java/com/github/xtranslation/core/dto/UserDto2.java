package com.github.xtranslation.core.dto;


import com.github.xtranslation.core.TeacherTrans;
import com.github.xtranslation.core.annotation.DictTrans;
import com.github.xtranslation.core.annotation.Trans;
import com.github.xtranslation.core.repository.SubjectTransRepository;
import lombok.Data;

import java.util.List;

@Data
public class UserDto2 {

    private Long id;

    private String name;

    private List<Long> teacherIds;

    private List<String> jobIds;

    @DictTrans(trans = "jobIds", group = "jobDict")
    private List<String> jobNames;

    @TeacherTrans(trans = "teacherIds", key = "name")
    private List<String> teacherName;

    @TeacherTrans(trans = "teacherIds", key = "subjectId")
    private List<Long> subjectIds;

    @Trans(repository = SubjectTransRepository.class, transKey = "subjectIds", transField = "name")
    private List<String> subjectNames;

    public UserDto2(Long id, String name, List<Long> teacherIds, List<String> jobIds) {
        this.id = id;
        this.name = name;
        this.teacherIds = teacherIds;
        this.jobIds = jobIds;
    }
}
