package com.acme.transport.safety.coaching.review

/** A rule a manual coaching ledger must pass. Check returns its problems, or none. */
interface ManualCoachingLedgerRule {
    fun check(item: ManualCoachingLedger): MutableList<String>
}

data class ManualCoachingLedgerDescriptionLengthRule(var maxLength: Int) : ManualCoachingLedgerRule {
    override fun check(item: ManualCoachingLedger): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCoachingLedgerDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : ManualCoachingLedgerRule {
    override fun check(item: ManualCoachingLedger): MutableList<String> {
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

data class ManualCoachingLedgerDurationMinutesRangeRule(var min: Int, var max: Int) : ManualCoachingLedgerRule {
    override fun check(item: ManualCoachingLedger): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualCoachingLedgerRequiredDescriptionRule(var message: String) : ManualCoachingLedgerRule {
    override fun check(item: ManualCoachingLedger): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual coaching ledger. */
data class ManualCoachingLedgerValidator(var rules: MutableList<ManualCoachingLedgerRule>) {
    fun validate(item: ManualCoachingLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualCoachingLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCoachingLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCoachingLedgerValidator(): ManualCoachingLedgerValidator {
    return ManualCoachingLedgerValidator(
        mutableListOf<ManualCoachingLedgerRule>(
            ManualCoachingLedgerDescriptionLengthRule(120),
            ManualCoachingLedgerDistanceKmRangeRule(5.0, 4696.4),
            ManualCoachingLedgerDurationMinutesRangeRule(3600, 86400),
            ManualCoachingLedgerRequiredDescriptionRule("manual coaching ledger needs this field"),
        ),
    )
}
