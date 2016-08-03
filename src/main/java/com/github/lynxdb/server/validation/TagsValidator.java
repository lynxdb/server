/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lynxdb.server.validation;

import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author honorem
 */
public class TagsValidator implements ConstraintValidator<Tags, Map<String, String>> {

    @Override
    public void initialize(Tags constraintAnnotation) {

    }

    @Override
    public boolean isValid(Map<String, String> value, ConstraintValidatorContext context) {
        return value
                .entrySet()
                .stream()
                .noneMatch((Map.Entry<String, String> t) -> t.getKey().contains("\\=") || t.getValue().contains("\\;"));
    }

}
