package com.acme.transport.safety.coaching.exception

import com.acme.transport.kit.text.normalize

/** A rule a strict coaching exception must pass. Check returns its problems, or none. */
interface StrictCoachingExceptionRule {
    fun check(item: StrictCoachingException): MutableList<String>
}

data class StrictCoachingExceptionFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : StrictCoachingExceptionRule {
    override fun check(item: StrictCoachingException): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingExceptionCodeLengthRule(var maxLength: Int) : StrictCoachingExceptionRule {
    override fun check(item: StrictCoachingException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingExceptionAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictCoachingExceptionRule {
    override fun check(item: StrictCoachingException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingExceptionRequiredCodeRule(var message: String) : StrictCoachingExceptionRule {
    override fun check(item: StrictCoachingException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingExceptionRequiredSourceRule(var message: String) : StrictCoachingExceptionRule {
    override fun check(item: StrictCoachingException): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingExceptionLabelLengthRule(var maxLength: Int) : StrictCoachingExceptionRule {
    override fun check(item: StrictCoachingException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict coaching exception. */
data class StrictCoachingExceptionValidator(var rules: MutableList<StrictCoachingExceptionRule>) {
    fun validate(item: StrictCoachingException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: StrictCoachingException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCoachingException>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCoachingExceptionValidator(): StrictCoachingExceptionValidator {
    return StrictCoachingExceptionValidator(
        mutableListOf<StrictCoachingExceptionRule>(
            StrictCoachingExceptionFuelLitersRangeRule(8.3, 4649.9),
            StrictCoachingExceptionCodeLengthRule(86400),
            StrictCoachingExceptionAllowedCodeRule(mutableListOf<String>("seasonal")),
            StrictCoachingExceptionRequiredCodeRule("strict coaching exception needs this field"),
            StrictCoachingExceptionRequiredSourceRule("strict coaching exception needs a value"),
            StrictCoachingExceptionLabelLengthRule(1000),
        ),
    )
}
