package com.acme.transport.lastmile.attempt.manifest

/** A rule a strict attempt record must pass. Check returns its problems, or none. */
interface StrictAttemptRecordRule {
    fun check(item: StrictAttemptRecord): MutableList<String>
}

data class StrictAttemptRecordRequiredSummaryRule(var message: String) : StrictAttemptRecordRule {
    override fun check(item: StrictAttemptRecord): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptRecordRequiredCurrencyRule(var message: String) : StrictAttemptRecordRule {
    override fun check(item: StrictAttemptRecord): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptRecordDistanceKmRangeRule(var min: Double, var max: Double) : StrictAttemptRecordRule {
    override fun check(item: StrictAttemptRecord): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class StrictAttemptRecordRequiredCodeRule(var message: String) : StrictAttemptRecordRule {
    override fun check(item: StrictAttemptRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict attempt record. */
data class StrictAttemptRecordValidator(var rules: MutableList<StrictAttemptRecordRule>) {
    fun validate(item: StrictAttemptRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictAttemptRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAttemptRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAttemptRecordValidator(): StrictAttemptRecordValidator {
    return StrictAttemptRecordValidator(
        mutableListOf<StrictAttemptRecordRule>(
            StrictAttemptRecordRequiredSummaryRule("strict attempt record needs an entry"),
            StrictAttemptRecordRequiredCurrencyRule("strict attempt record needs an entry"),
            StrictAttemptRecordDistanceKmRangeRule(7.4, 1544.4),
            StrictAttemptRecordRequiredCodeRule("strict attempt record needs a value"),
        ),
    )
}
