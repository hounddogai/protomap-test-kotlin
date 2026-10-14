package com.acme.transport.customs.hold.batch

/** A rule a legacy hold exception must pass. Check returns its problems, or none. */
interface LegacyHoldExceptionRule {
    fun check(item: LegacyHoldException): MutableList<String>
}

data class LegacyHoldExceptionDistanceKmRangeRule(var min: Double, var max: Double) : LegacyHoldExceptionRule {
    override fun check(item: LegacyHoldException): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyHoldExceptionRequiredNotesRule(var message: String) : LegacyHoldExceptionRule {
    override fun check(item: LegacyHoldException): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyHoldExceptionCodeLengthRule(var maxLength: Int) : LegacyHoldExceptionRule {
    override fun check(item: LegacyHoldException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy hold exception. */
data class LegacyHoldExceptionValidator(var rules: MutableList<LegacyHoldExceptionRule>) {
    fun validate(item: LegacyHoldException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyHoldException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyHoldException>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyHoldExceptionValidator(): LegacyHoldExceptionValidator {
    return LegacyHoldExceptionValidator(
        mutableListOf<LegacyHoldExceptionRule>(
            LegacyHoldExceptionDistanceKmRangeRule(8.1, 2111.4),
            LegacyHoldExceptionRequiredNotesRule("legacy hold exception needs an entry"),
            LegacyHoldExceptionCodeLengthRule(32),
        ),
    )
}
