package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.text.normalize

/** A rule a capped route offset metric must pass. Check returns its problems, or none. */
interface CappedRouteOffsetMetricRule {
    fun check(item: CappedRouteOffsetMetric): MutableList<String>
}

data class CappedRouteOffsetMetricLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : CappedRouteOffsetMetricRule {
    override fun check(item: CappedRouteOffsetMetric): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetMetricBatchSizeRangeRule(var min: Int, var max: Int) : CappedRouteOffsetMetricRule {
    override fun check(item: CappedRouteOffsetMetric): MutableList<String> {
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

data class CappedRouteOffsetMetricAllowedUnitRule(
    var allowed: MutableList<String>,
) : CappedRouteOffsetMetricRule {
    override fun check(item: CappedRouteOffsetMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetMetricNameLengthRule(var maxLength: Int) : CappedRouteOffsetMetricRule {
    override fun check(item: CappedRouteOffsetMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetMetricRequiredUnitRule(var message: String) : CappedRouteOffsetMetricRule {
    override fun check(item: CappedRouteOffsetMetric): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetMetricTitleLengthRule(var maxLength: Int) : CappedRouteOffsetMetricRule {
    override fun check(item: CappedRouteOffsetMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped route offset metric. */
data class CappedRouteOffsetMetricValidator(var rules: MutableList<CappedRouteOffsetMetricRule>) {
    fun validate(item: CappedRouteOffsetMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedRouteOffsetMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRouteOffsetMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRouteOffsetMetricValidator(): CappedRouteOffsetMetricValidator {
    return CappedRouteOffsetMetricValidator(
        mutableListOf<CappedRouteOffsetMetricRule>(
            CappedRouteOffsetMetricLoadKgRangeRule(5.8, 3340.5),
            CappedRouteOffsetMetricBatchSizeRangeRule(32, 1000),
            CappedRouteOffsetMetricAllowedUnitRule(mutableListOf<String>()),
            CappedRouteOffsetMetricNameLengthRule(80),
            CappedRouteOffsetMetricRequiredUnitRule("capped route offset metric needs a value"),
            CappedRouteOffsetMetricTitleLengthRule(255),
        ),
    )
}
