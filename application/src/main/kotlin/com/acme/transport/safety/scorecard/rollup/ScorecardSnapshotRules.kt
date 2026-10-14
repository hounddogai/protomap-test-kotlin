package com.acme.transport.safety.scorecard.rollup

/** A rule a scorecard snapshot must pass. Check returns its problems, or none. */
interface ScorecardSnapshotRule {
    fun check(item: ScorecardSnapshot): MutableList<String>
}

data class ScorecardSnapshotCeilingRangeRule(var min: Double, var max: Double) : ScorecardSnapshotRule {
    override fun check(item: ScorecardSnapshot): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardSnapshotRequiredLabelRule(var message: String) : ScorecardSnapshotRule {
    override fun check(item: ScorecardSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardSnapshotWindowDaysRangeRule(var min: Int, var max: Int) : ScorecardSnapshotRule {
    override fun check(item: ScorecardSnapshot): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class ScorecardSnapshotCodeLengthRule(var maxLength: Int) : ScorecardSnapshotRule {
    override fun check(item: ScorecardSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard snapshot. */
data class ScorecardSnapshotValidator(var rules: MutableList<ScorecardSnapshotRule>) {
    fun validate(item: ScorecardSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardSnapshotValidator(): ScorecardSnapshotValidator {
    return ScorecardSnapshotValidator(
        mutableListOf<ScorecardSnapshotRule>(
            ScorecardSnapshotCeilingRangeRule(3.2, 2608.3),
            ScorecardSnapshotRequiredLabelRule("scorecard snapshot needs a value"),
            ScorecardSnapshotWindowDaysRangeRule(64, 1000),
            ScorecardSnapshotCodeLengthRule(80),
        ),
    )
}
