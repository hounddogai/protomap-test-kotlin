package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.normalize

/** A rule a default inspection estimate must pass. Check returns its problems, or none. */
interface DefaultInspectionEstimateRule {
    fun check(item: DefaultInspectionEstimate): MutableList<String>
}

data class DefaultInspectionEstimateAllowedDepotRule(
    var allowed: MutableList<String>,
) : DefaultInspectionEstimateRule {
    override fun check(item: DefaultInspectionEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionEstimateStopCountRangeRule(
    var min: Int,
    var max: Int,
) : DefaultInspectionEstimateRule {
    override fun check(item: DefaultInspectionEstimate): MutableList<String> {
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

data class DefaultInspectionEstimateRequiredDepotRule(var message: String) : DefaultInspectionEstimateRule {
    override fun check(item: DefaultInspectionEstimate): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default inspection estimate. */
data class DefaultInspectionEstimateValidator(var rules: MutableList<DefaultInspectionEstimateRule>) {
    fun validate(item: DefaultInspectionEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultInspectionEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultInspectionEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultInspectionEstimateValidator(): DefaultInspectionEstimateValidator {
    return DefaultInspectionEstimateValidator(
        mutableListOf<DefaultInspectionEstimateRule>(
            DefaultInspectionEstimateAllowedDepotRule(mutableListOf<String>("default", "fallback")),
            DefaultInspectionEstimateStopCountRangeRule(16, 32),
            DefaultInspectionEstimateRequiredDepotRule("default inspection estimate needs this field"),
        ),
    )
}
