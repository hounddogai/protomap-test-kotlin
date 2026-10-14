package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.kit.text.normalize

/** A rule a regional qualification exception must pass. Check returns its problems, or none. */
interface RegionalQualificationExceptionRule {
    fun check(item: RegionalQualificationException): MutableList<String>
}

data class RegionalQualificationExceptionCeilingRangeRule(
    var min: Double,
    var max: Double,
) : RegionalQualificationExceptionRule {
    override fun check(item: RegionalQualificationException): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationExceptionCodeLengthRule(
    var maxLength: Int,
) : RegionalQualificationExceptionRule {
    override fun check(item: RegionalQualificationException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationExceptionRequiredCodeRule(
    var message: String,
) : RegionalQualificationExceptionRule {
    override fun check(item: RegionalQualificationException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationExceptionLabelLengthRule(
    var maxLength: Int,
) : RegionalQualificationExceptionRule {
    override fun check(item: RegionalQualificationException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationExceptionRequiredLabelRule(
    var message: String,
) : RegionalQualificationExceptionRule {
    override fun check(item: RegionalQualificationException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationExceptionAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalQualificationExceptionRule {
    override fun check(item: RegionalQualificationException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional qualification exception. */
data class RegionalQualificationExceptionValidator(var rules: MutableList<RegionalQualificationExceptionRule>) {
    fun validate(item: RegionalQualificationException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalQualificationException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalQualificationException>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultRegionalQualificationExceptionValidator(): RegionalQualificationExceptionValidator {
    return RegionalQualificationExceptionValidator(
        mutableListOf<RegionalQualificationExceptionRule>(
            RegionalQualificationExceptionCeilingRangeRule(7.4, 2987.6),
            RegionalQualificationExceptionCodeLengthRule(16),
            RegionalQualificationExceptionRequiredCodeRule("regional qualification exception needs a value"),
            RegionalQualificationExceptionLabelLengthRule(16),
            RegionalQualificationExceptionRequiredLabelRule("regional qualification exception needs this field"),
            RegionalQualificationExceptionAllowedCodeRule(mutableListOf<String>("scheduled")),
        ),
    )
}
