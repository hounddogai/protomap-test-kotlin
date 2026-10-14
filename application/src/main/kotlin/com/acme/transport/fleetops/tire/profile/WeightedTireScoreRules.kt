package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.text.normalize

/** A rule a weighted tire score must pass. Check returns its problems, or none. */
interface WeightedTireScoreRule {
    fun check(item: WeightedTireScore): MutableList<String>
}

data class WeightedTireScorePriorityRangeRule(var min: Int, var max: Int) : WeightedTireScoreRule {
    override fun check(item: WeightedTireScore): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class WeightedTireScoreAllowedCarrierRule(var allowed: MutableList<String>) : WeightedTireScoreRule {
    override fun check(item: WeightedTireScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireScoreLabelLengthRule(var maxLength: Int) : WeightedTireScoreRule {
    override fun check(item: WeightedTireScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireScoreRequiredCarrierRule(var message: String) : WeightedTireScoreRule {
    override fun check(item: WeightedTireScore): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireScoreCodeLengthRule(var maxLength: Int) : WeightedTireScoreRule {
    override fun check(item: WeightedTireScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted tire score. */
data class WeightedTireScoreValidator(var rules: MutableList<WeightedTireScoreRule>) {
    fun validate(item: WeightedTireScore): MutableList<String> {
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

    fun requireValid(item: WeightedTireScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTireScore>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTireScoreValidator(): WeightedTireScoreValidator {
    return WeightedTireScoreValidator(
        mutableListOf<WeightedTireScoreRule>(
            WeightedTireScorePriorityRangeRule(80, 86400),
            WeightedTireScoreAllowedCarrierRule(mutableListOf<String>("primary")),
            WeightedTireScoreLabelLengthRule(86400),
            WeightedTireScoreRequiredCarrierRule("weighted tire score needs this field"),
            WeightedTireScoreCodeLengthRule(16),
        ),
    )
}
