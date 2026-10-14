package com.acme.transport.lastmile.attempt.template

/** A rule a capped attempt exception must pass. Check returns its problems, or none. */
interface CappedAttemptExceptionRule {
    fun check(item: CappedAttemptException): MutableList<String>
}

data class CappedAttemptExceptionCodeLengthRule(var maxLength: Int) : CappedAttemptExceptionRule {
    override fun check(item: CappedAttemptException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptExceptionRequiredCodeRule(var message: String) : CappedAttemptExceptionRule {
    override fun check(item: CappedAttemptException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptExceptionVersionRangeRule(var min: Int, var max: Int) : CappedAttemptExceptionRule {
    override fun check(item: CappedAttemptException): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped attempt exception. */
data class CappedAttemptExceptionValidator(var rules: MutableList<CappedAttemptExceptionRule>) {
    fun validate(item: CappedAttemptException): MutableList<String> {
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

    fun requireValid(item: CappedAttemptException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAttemptException>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAttemptExceptionValidator(): CappedAttemptExceptionValidator {
    return CappedAttemptExceptionValidator(
        mutableListOf<CappedAttemptExceptionRule>(
            CappedAttemptExceptionCodeLengthRule(120),
            CappedAttemptExceptionRequiredCodeRule("capped attempt exception needs this field"),
            CappedAttemptExceptionVersionRangeRule(120, 1000),
        ),
    )
}
