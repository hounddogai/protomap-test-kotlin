package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a weighted dwell variance must pass. Check returns its problems, or none. */
interface WeightedDwellVarianceRule {
    fun check(item: WeightedDwellVariance): MutableList<String>
}

data class WeightedDwellVarianceStopCountRangeRule(var min: Int, var max: Int) : WeightedDwellVarianceRule {
    override fun check(item: WeightedDwellVariance): MutableList<String> {
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

data class WeightedDwellVarianceAllowedTitleRule(var allowed: MutableList<String>) : WeightedDwellVarianceRule {
    override fun check(item: WeightedDwellVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellVarianceCodeLengthRule(var maxLength: Int) : WeightedDwellVarianceRule {
    override fun check(item: WeightedDwellVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellVarianceRequiredDriverIdRule(var message: String) : WeightedDwellVarianceRule {
    override fun check(item: WeightedDwellVariance): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted dwell variance. */
data class WeightedDwellVarianceValidator(var rules: MutableList<WeightedDwellVarianceRule>) {
    fun validate(item: WeightedDwellVariance): MutableList<String> {
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

    fun requireValid(item: WeightedDwellVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDwellVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDwellVarianceValidator(): WeightedDwellVarianceValidator {
    return WeightedDwellVarianceValidator(
        mutableListOf<WeightedDwellVarianceRule>(
            WeightedDwellVarianceStopCountRangeRule(120, 120),
            WeightedDwellVarianceAllowedTitleRule(mutableListOf<String>("seasonal", "manual", "weighted")),
            WeightedDwellVarianceCodeLengthRule(32),
            WeightedDwellVarianceRequiredDriverIdRule("weighted dwell variance needs an entry"),
        ),
    )
}
