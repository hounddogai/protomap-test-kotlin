package com.acme.transport.network.transittime.metric

/** A rule a primary transit time metric must pass. Check returns its problems, or none. */
interface PrimaryTransitTimeMetricRule {
    fun check(item: PrimaryTransitTimeMetric): MutableList<String>
}

data class PrimaryTransitTimeMetricRequiredCodeRule(var message: String) : PrimaryTransitTimeMetricRule {
    override fun check(item: PrimaryTransitTimeMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeMetricRequiredCountryRule(var message: String) : PrimaryTransitTimeMetricRule {
    override fun check(item: PrimaryTransitTimeMetric): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeMetricStopCountRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryTransitTimeMetricRule {
    override fun check(item: PrimaryTransitTimeMetric): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary transit time metric. */
data class PrimaryTransitTimeMetricValidator(var rules: MutableList<PrimaryTransitTimeMetricRule>) {
    fun validate(item: PrimaryTransitTimeMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryTransitTimeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTransitTimeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTransitTimeMetricValidator(): PrimaryTransitTimeMetricValidator {
    return PrimaryTransitTimeMetricValidator(
        mutableListOf<PrimaryTransitTimeMetricRule>(
            PrimaryTransitTimeMetricRequiredCodeRule("primary transit time metric needs a value"),
            PrimaryTransitTimeMetricRequiredCountryRule("primary transit time metric needs this field"),
            PrimaryTransitTimeMetricStopCountRangeRule(64, 3600),
        ),
    )
}
