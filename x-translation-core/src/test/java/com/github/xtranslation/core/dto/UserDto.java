package com.github.xtranslation.core.dto;


import com.github.xtranslation.core.annotation.DictTrans;
import com.github.xtranslation.core.annotation.Trans;
import com.github.xtranslation.core.repository.SubjectTransRepository;
import com.github.xtranslation.core.repository.TeacherTransRepository;
import lombok.Data;

@Data
public class UserDto {

    private Long id;

    private String name;

    private String sex;

    @DictTrans(trans = "sex", group = "sexDict")
    private String sexName;

    private String job;

    @DictTrans(trans = "job", group = "jobDict")
    private String jobName;

    private Long teacherId;

    @Trans(transKey = "teacherId", transField = "name", repository = TeacherTransRepository.class)
    private String teacherName;

    @Trans(transKey = "teacherId", transField = "subjectId", repository = TeacherTransRepository.class)
    private Long subjectId;

    @Trans(transKey = "subjectId", repository = SubjectTransRepository.class, transField = "name")
    private String subjectName;

    public UserDto(Long id, String name, Long teacherId, String sex, String job) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.sex = sex;
        this.job = job;
    }
}
