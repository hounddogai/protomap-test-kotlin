package com.acme.transport.crewing.restbreak.exception

/** A rule a rest break exception must pass. Check returns its problems, or none. */
interface RestBreakExceptionRule {
    fun check(item: RestBreakException): MutableList<String>
}

data class RestBreakExceptionMinimumRangeRule(var min: Double, var max: Double) : RestBreakExceptionRule {
    override fun check(item: RestBreakException): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakExceptionStartsAtRangeRule(var min: Int, var max: Int) : RestBreakExceptionRule {
    override fun check(item: RestBreakException): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class RestBreakExceptionDriverIdLengthRule(var maxLength: Int) : RestBreakExceptionRule {
    override fun check(item: RestBreakException): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakExceptionCodeLengthRule(var maxLength: Int) : RestBreakExceptionRule {
    override fun check(item: RestBreakException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break exception. */
data class RestBreakExceptionValidator(var rules: MutableList<RestBreakExceptionRule>) {
    fun validate(item: RestBreakException): MutableList<String> {
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

    fun requireValid(item: RestBreakException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakException>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakExceptionValidator(): RestBreakExceptionValidator {
    return RestBreakExceptionValidator(
        mutableListOf<RestBreakExceptionRule>(
            RestBreakExceptionMinimumRangeRule(7.2, 2857.7),
            RestBreakExceptionStartsAtRangeRule(16, 32),
            RestBreakExceptionDriverIdLengthRule(255),
            RestBreakExceptionCodeLengthRule(16),
        ),
    )
}
