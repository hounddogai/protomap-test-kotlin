package com.acme.transport.safety.violation.forecast

/** A rule a lenient violation exception must pass. Check returns its problems, or none. */
interface LenientViolationExceptionRule {
    fun check(item: LenientViolationException): MutableList<String>
}

data class LenientViolationExceptionAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : LenientViolationExceptionRule {
    override fun check(item: LenientViolationException): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationExceptionStopCountRangeRule(
    var min: Int,
    var max: Int,
) : LenientViolationExceptionRule {
    override fun check(item: LenientViolationException): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class LenientViolationExceptionRequiredTimezoneRule(var message: String) : LenientViolationExceptionRule {
    override fun check(item: LenientViolationException): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationExceptionCodeLengthRule(var maxLength: Int) : LenientViolationExceptionRule {
    override fun check(item: LenientViolationException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient violation exception. */
data class LenientViolationExceptionValidator(var rules: MutableList<LenientViolationExceptionRule>) {
    fun validate(item: LenientViolationException): MutableList<String> {
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

    fun requireValid(item: LenientViolationException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientViolationException>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientViolationExceptionValidator(): LenientViolationExceptionValidator {
    return LenientViolationExceptionValidator(
        mutableListOf<LenientViolationExceptionRule>(
            LenientViolationExceptionAttemptsRangeRule(255, 3600),
            LenientViolationExceptionStopCountRangeRule(16, 16),
            LenientViolationExceptionRequiredTimezoneRule("lenient violation exception needs this field"),
            LenientViolationExceptionCodeLengthRule(1000),
        ),
    )
}
