package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.text.normalize

/** A rule a scorecard window must pass. Check returns its problems, or none. */
interface ScorecardWindowRule {
    fun check(item: ScorecardWindow): MutableList<String>
}

data class ScorecardWindowAllowedTitleRule(var allowed: MutableList<String>) : ScorecardWindowRule {
    override fun check(item: ScorecardWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardWindowTrailerIdLengthRule(var maxLength: Int) : ScorecardWindowRule {
    override fun check(item: ScorecardWindow): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardWindowRegionLengthRule(var maxLength: Int) : ScorecardWindowRule {
    override fun check(item: ScorecardWindow): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardWindowDiscountRangeRule(var min: Double, var max: Double) : ScorecardWindowRule {
    override fun check(item: ScorecardWindow): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scorecard window. */
data class ScorecardWindowValidator(var rules: MutableList<ScorecardWindowRule>) {
    fun validate(item: ScorecardWindow): MutableList<String> {
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

    fun requireValid(item: ScorecardWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardWindowValidator(): ScorecardWindowValidator {
    return ScorecardWindowValidator(
        mutableListOf<ScorecardWindowRule>(
            ScorecardWindowAllowedTitleRule(mutableListOf<String>("manual", "seasonal", "seasonal")),
            ScorecardWindowTrailerIdLengthRule(80),
            ScorecardWindowRegionLengthRule(16),
            ScorecardWindowDiscountRangeRule(3.2, 4532.7),
        ),
    )
}
