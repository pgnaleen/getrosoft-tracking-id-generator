package com.getrosoft.com.getrosoftgenerateid.utils.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class IsoCountryCodeValidator implements ConstraintValidator<ValidIsoCountryCode, String> {
    private static final Set<String> ISO_COUNTRY_CODES =
            Arrays.stream(Locale.getISOCountries()).collect(Collectors.toSet());

    @Override
    public boolean isValid(String country, ConstraintValidatorContext context) {
        return ISO_COUNTRY_CODES.contains(country);
    }
 }
