package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.text.normalize

/** A rule a regional service area threshold must pass. Check returns its problems, or none. */
interface RegionalServiceAreaThresholdRule {
    fun check(item: RegionalServiceAreaThreshold): MutableList<String>
}

data class RegionalServiceAreaThresholdMarginRangeRule(
    var min: Double,
    var max: Double,
) : RegionalServiceAreaThresholdRule {
    override fun check(item: RegionalServiceAreaThreshold): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaThresholdRequiredCodeRule(
    var message: String,
) : RegionalServiceAreaThresholdRule {
    override fun check(item: RegionalServiceAreaThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaThresholdScoreRangeRule(
    var min: Double,
    var max: Double,
) : RegionalServiceAreaThresholdRule {
    override fun check(item: RegionalServiceAreaThreshold): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class RegionalServiceAreaThresholdAllowedCategoryRule(
    var allowed: MutableList<String>,
) : RegionalServiceAreaThresholdRule {
    override fun check(item: RegionalServiceAreaThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional service area threshold. */
data class RegionalServiceAreaThresholdValidator(var rules: MutableList<RegionalServiceAreaThresholdRule>) {
    fun validate(item: RegionalServiceAreaThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalServiceAreaThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalServiceAreaThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalServiceAreaThresholdValidator(): RegionalServiceAreaThresholdValidator {
    return RegionalServiceAreaThresholdValidator(
        mutableListOf<RegionalServiceAreaThresholdRule>(
            RegionalServiceAreaThresholdMarginRangeRule(5.8, 2565.9),
            RegionalServiceAreaThresholdRequiredCodeRule("regional service area threshold needs a value"),
            RegionalServiceAreaThresholdScoreRangeRule(9.0, 74.8),
            RegionalServiceAreaThresholdAllowedCategoryRule(mutableListOf<String>("primary", "primary")),
        ),
    )
}
