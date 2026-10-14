package com.acme.transport.safety.scorecard.scorecardwindow

/** A rule a lenient scorecard window must pass. Check returns its problems, or none. */
interface LenientScorecardWindowRule {
    fun check(item: LenientScorecardWindow): MutableList<String>
}

data class LenientScorecardWindowRequiredCodeRule(var message: String) : LenientScorecardWindowRule {
    override fun check(item: LenientScorecardWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientScorecardWindowExpiresAtRangeRule(var min: Int, var max: Int) : LenientScorecardWindowRule {
    override fun check(item: LenientScorecardWindow): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class LenientScorecardWindowDescriptionLengthRule(var maxLength: Int) : LenientScorecardWindowRule {
    override fun check(item: LenientScorecardWindow): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientScorecardWindowRequiredSegmentRule(var message: String) : LenientScorecardWindowRule {
    override fun check(item: LenientScorecardWindow): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient scorecard window. */
data class LenientScorecardWindowValidator(var rules: MutableList<LenientScorecardWindowRule>) {
    fun validate(item: LenientScorecardWindow): MutableList<String> {
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

    fun requireValid(item: LenientScorecardWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientScorecardWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientScorecardWindowValidator(): LenientScorecardWindowValidator {
    return LenientScorecardWindowValidator(
        mutableListOf<LenientScorecardWindowRule>(
            LenientScorecardWindowRequiredCodeRule("lenient scorecard window needs a value"),
            LenientScorecardWindowExpiresAtRangeRule(16, 120),
            LenientScorecardWindowDescriptionLengthRule(80),
            LenientScorecardWindowRequiredSegmentRule("lenient scorecard window needs this field"),
        ),
    )
}
