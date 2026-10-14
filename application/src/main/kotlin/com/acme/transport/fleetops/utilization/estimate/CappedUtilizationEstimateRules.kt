package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.kit.text.normalize

/** A rule a capped utilization estimate must pass. Check returns its problems, or none. */
interface CappedUtilizationEstimateRule {
    fun check(item: CappedUtilizationEstimate): MutableList<String>
}

data class CappedUtilizationEstimateMinimumRangeRule(
    var min: Double,
    var max: Double,
) : CappedUtilizationEstimateRule {
    override fun check(item: CappedUtilizationEstimate): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class CappedUtilizationEstimateLabelLengthRule(var maxLength: Int) : CappedUtilizationEstimateRule {
    override fun check(item: CappedUtilizationEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedUtilizationEstimateAllowedCodeRule(
    var allowed: MutableList<String>,
) : CappedUtilizationEstimateRule {
    override fun check(item: CappedUtilizationEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CappedUtilizationEstimateRequiredDriverIdRule(var message: String) : CappedUtilizationEstimateRule {
    override fun check(item: CappedUtilizationEstimate): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped utilization estimate. */
data class CappedUtilizationEstimateValidator(var rules: MutableList<CappedUtilizationEstimateRule>) {
    fun validate(item: CappedUtilizationEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedUtilizationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedUtilizationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedUtilizationEstimateValidator(): CappedUtilizationEstimateValidator {
    return CappedUtilizationEstimateValidator(
        mutableListOf<CappedUtilizationEstimateRule>(
            CappedUtilizationEstimateMinimumRangeRule(0.4, 2510.4),
            CappedUtilizationEstimateLabelLengthRule(120),
            CappedUtilizationEstimateAllowedCodeRule(mutableListOf<String>("fallback", "legacy", "adaptive")),
            CappedUtilizationEstimateRequiredDriverIdRule("capped utilization estimate needs a value"),
        ),
    )
}
