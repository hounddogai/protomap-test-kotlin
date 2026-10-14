package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a weighted service plan entry must pass. Check returns its problems, or none. */
interface WeightedServicePlanEntryRule {
    fun check(item: WeightedServicePlanEntry): MutableList<String>
}

data class WeightedServicePlanEntryBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : WeightedServicePlanEntryRule {
    override fun check(item: WeightedServicePlanEntry): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class WeightedServicePlanEntryAllowedCodeRule(
    var allowed: MutableList<String>,
) : WeightedServicePlanEntryRule {
    override fun check(item: WeightedServicePlanEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanEntryVersionRangeRule(var min: Int, var max: Int) : WeightedServicePlanEntryRule {
    override fun check(item: WeightedServicePlanEntry): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted service plan entry. */
data class WeightedServicePlanEntryValidator(var rules: MutableList<WeightedServicePlanEntryRule>) {
    fun validate(item: WeightedServicePlanEntry): MutableList<String> {
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

    fun requireValid(item: WeightedServicePlanEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedServicePlanEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedServicePlanEntryValidator(): WeightedServicePlanEntryValidator {
    return WeightedServicePlanEntryValidator(
        mutableListOf<WeightedServicePlanEntryRule>(
            WeightedServicePlanEntryBatchSizeRangeRule(64, 1000),
            WeightedServicePlanEntryAllowedCodeRule(mutableListOf<String>("regional", "weighted", "manual")),
            WeightedServicePlanEntryVersionRangeRule(120, 120),
        ),
    )
}
