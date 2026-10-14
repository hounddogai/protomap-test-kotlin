package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.text.normalize

/** A rule a default tire estimate must pass. Check returns its problems, or none. */
interface DefaultTireEstimateRule {
    fun check(item: DefaultTireEstimate): MutableList<String>
}

data class DefaultTireEstimateWindowDaysRangeRule(var min: Int, var max: Int) : DefaultTireEstimateRule {
    override fun check(item: DefaultTireEstimate): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTireEstimateThresholdRangeRule(var min: Double, var max: Double) : DefaultTireEstimateRule {
    override fun check(item: DefaultTireEstimate): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class DefaultTireEstimateAllowedReviewerRule(var allowed: MutableList<String>) : DefaultTireEstimateRule {
    override fun check(item: DefaultTireEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default tire estimate. */
data class DefaultTireEstimateValidator(var rules: MutableList<DefaultTireEstimateRule>) {
    fun validate(item: DefaultTireEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTireEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTireEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTireEstimateValidator(): DefaultTireEstimateValidator {
    return DefaultTireEstimateValidator(
        mutableListOf<DefaultTireEstimateRule>(
            DefaultTireEstimateWindowDaysRangeRule(16, 120),
            DefaultTireEstimateThresholdRangeRule(3.5, 180.0),
            DefaultTireEstimateAllowedReviewerRule(mutableListOf<String>("default", "batched")),
        ),
    )
}
