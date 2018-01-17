package com.xiaojinzi.form;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author 金全 wrj008
 * @version 1.0.0 2018/1/17.
 * @description
 */
@Data
public class ManForm {

    /** 主键 .*/
    private String id;

    /** 名字 .*/
    private String name;

    /** 国家 .*/
    private String countary;

    /** 出生日期 .*/
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    /** 年龄 .*/
    private Integer age;
}
