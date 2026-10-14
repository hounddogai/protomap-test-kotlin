package com.acme.transport.safety.scorecard.cycle

import com.acme.transport.kit.text.normalize

/** A rule a legacy scorecard cycle must pass. Check returns its problems, or none. */
interface LegacyScorecardCycleRule {
    fun check(item: LegacyScorecardCycle): MutableList<String>
}

data class LegacyScorecardCycleCostRangeRule(var min: Double, var max: Double) : LegacyScorecardCycleRule {
    override fun check(item: LegacyScorecardCycle): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class LegacyScorecardCycleAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : LegacyScorecardCycleRule {
    override fun check(item: LegacyScorecardCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LegacyScorecardCycleLabelLengthRule(var maxLength: Int) : LegacyScorecardCycleRule {
    override fun check(item: LegacyScorecardCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy scorecard cycle. */
data class LegacyScorecardCycleValidator(var rules: MutableList<LegacyScorecardCycleRule>) {
    fun validate(item: LegacyScorecardCycle): MutableList<String> {
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

    fun requireValid(item: LegacyScorecardCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyScorecardCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyScorecardCycleValidator(): LegacyScorecardCycleValidator {
    return LegacyScorecardCycleValidator(
        mutableListOf<LegacyScorecardCycleRule>(
            LegacyScorecardCycleCostRangeRule(3.3, 1787.0),
            LegacyScorecardCycleAllowedExternalRefRule(mutableListOf<String>("seasonal")),
            LegacyScorecardCycleLabelLengthRule(64),
        ),
    )
}
