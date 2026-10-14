package com.acme.transport.safety.scorecard.scorecardnotice

/** A rule a lenient scorecard snapshot must pass. Check returns its problems, or none. */
interface LenientScorecardSnapshotRule {
    fun check(item: LenientScorecardSnapshot): MutableList<String>
}

data class LenientScorecardSnapshotCodePrefixRule(var prefix: String) : LenientScorecardSnapshotRule {
    override fun check(item: LenientScorecardSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientScorecardSnapshotTitleLengthRule(var maxLength: Int) : LenientScorecardSnapshotRule {
    override fun check(item: LenientScorecardSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientScorecardSnapshotAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : LenientScorecardSnapshotRule {
    override fun check(item: LenientScorecardSnapshot): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientScorecardSnapshotRequiredTitleRule(var message: String) : LenientScorecardSnapshotRule {
    override fun check(item: LenientScorecardSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient scorecard snapshot. */
data class LenientScorecardSnapshotValidator(var rules: MutableList<LenientScorecardSnapshotRule>) {
    fun validate(item: LenientScorecardSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientScorecardSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientScorecardSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientScorecardSnapshotValidator(): LenientScorecardSnapshotValidator {
    return LenientScorecardSnapshotValidator(
        mutableListOf<LenientScorecardSnapshotRule>(
            LenientScorecardSnapshotCodePrefixRule("OPS"),
            LenientScorecardSnapshotTitleLengthRule(64),
            LenientScorecardSnapshotAttemptsRangeRule(32, 80),
            LenientScorecardSnapshotRequiredTitleRule("lenient scorecard snapshot needs an entry"),
        ),
    )
}
