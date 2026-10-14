package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.text.normalize

/** A rule a carbon report metric must pass. Check returns its problems, or none. */
interface CarbonReportMetricRule {
    fun check(item: CarbonReportMetric): MutableList<String>
}

data class CarbonReportMetricCeilingRangeRule(var min: Double, var max: Double) : CarbonReportMetricRule {
    override fun check(item: CarbonReportMetric): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportMetricRequiredSourceRule(var message: String) : CarbonReportMetricRule {
    override fun check(item: CarbonReportMetric): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportMetricAllowedCarrierRule(var allowed: MutableList<String>) : CarbonReportMetricRule {
    override fun check(item: CarbonReportMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportMetricSourceLengthRule(var maxLength: Int) : CarbonReportMetricRule {
    override fun check(item: CarbonReportMetric): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report metric. */
data class CarbonReportMetricValidator(var rules: MutableList<CarbonReportMetricRule>) {
    fun validate(item: CarbonReportMetric): MutableList<String> {
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

    fun requireValid(item: CarbonReportMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportMetricValidator(): CarbonReportMetricValidator {
    return CarbonReportMetricValidator(
        mutableListOf<CarbonReportMetricRule>(
            CarbonReportMetricCeilingRangeRule(4.8, 1911.3),
            CarbonReportMetricRequiredSourceRule("carbon report metric needs a value"),
            CarbonReportMetricAllowedCarrierRule(mutableListOf<String>("pending", "rolling", "batched")),
            CarbonReportMetricSourceLengthRule(1000),
        ),
    )
}
