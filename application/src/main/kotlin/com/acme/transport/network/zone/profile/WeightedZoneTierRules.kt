package com.acme.transport.network.zone.profile

import com.acme.transport.kit.text.normalize

/** A rule a weighted zone tier must pass. Check returns its problems, or none. */
interface WeightedZoneTierRule {
    fun check(item: WeightedZoneTier): MutableList<String>
}

data class WeightedZoneTierAllowedLabelRule(var allowed: MutableList<String>) : WeightedZoneTierRule {
    override fun check(item: WeightedZoneTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class WeightedZoneTierCodeLengthRule(var maxLength: Int) : WeightedZoneTierRule {
    override fun check(item: WeightedZoneTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedZoneTierStopCountRangeRule(var min: Int, var max: Int) : WeightedZoneTierRule {
    override fun check(item: WeightedZoneTier): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted zone tier. */
data class WeightedZoneTierValidator(var rules: MutableList<WeightedZoneTierRule>) {
    fun validate(item: WeightedZoneTier): MutableList<String> {
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

    fun requireValid(item: WeightedZoneTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedZoneTier>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedZoneTierValidator(): WeightedZoneTierValidator {
    return WeightedZoneTierValidator(
        mutableListOf<WeightedZoneTierRule>(
            WeightedZoneTierAllowedLabelRule(mutableListOf<String>("fallback", "strict", "scheduled")),
            WeightedZoneTierCodeLengthRule(64),
            WeightedZoneTierStopCountRangeRule(16, 3600),
        ),
    )
}
