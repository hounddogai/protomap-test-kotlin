package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.text.normalize

/** A rule a primary tender exception must pass. Check returns its problems, or none. */
interface PrimaryTenderExceptionRule {
    fun check(item: PrimaryTenderException): MutableList<String>
}

data class PrimaryTenderExceptionAttemptsRangeRule(var min: Int, var max: Int) : PrimaryTenderExceptionRule {
    override fun check(item: PrimaryTenderException): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class PrimaryTenderExceptionAllowedTitleRule(
    var allowed: MutableList<String>,
) : PrimaryTenderExceptionRule {
    override fun check(item: PrimaryTenderException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderExceptionCountryLengthRule(var maxLength: Int) : PrimaryTenderExceptionRule {
    override fun check(item: PrimaryTenderException): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tender exception. */
data class PrimaryTenderExceptionValidator(var rules: MutableList<PrimaryTenderExceptionRule>) {
    fun validate(item: PrimaryTenderException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryTenderException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTenderException>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTenderExceptionValidator(): PrimaryTenderExceptionValidator {
    return PrimaryTenderExceptionValidator(
        mutableListOf<PrimaryTenderExceptionRule>(
            PrimaryTenderExceptionAttemptsRangeRule(32, 255),
            PrimaryTenderExceptionAllowedTitleRule(mutableListOf<String>()),
            PrimaryTenderExceptionCountryLengthRule(120),
        ),
    )
}
