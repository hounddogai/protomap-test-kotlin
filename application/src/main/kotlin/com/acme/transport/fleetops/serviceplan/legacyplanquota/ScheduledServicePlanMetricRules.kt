package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.text.normalize

/** A rule a scheduled service plan metric must pass. Check returns its problems, or none. */
interface ScheduledServicePlanMetricRule {
    fun check(item: ScheduledServicePlanMetric): MutableList<String>
}

data class ScheduledServicePlanMetricRequiredCountryRule(var message: String) : ScheduledServicePlanMetricRule {
    override fun check(item: ScheduledServicePlanMetric): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServicePlanMetricLimitRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledServicePlanMetricRule {
    override fun check(item: ScheduledServicePlanMetric): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServicePlanMetricRequiredCodeRule(var message: String) : ScheduledServicePlanMetricRule {
    override fun check(item: ScheduledServicePlanMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServicePlanMetricTitleLengthRule(var maxLength: Int) : ScheduledServicePlanMetricRule {
    override fun check(item: ScheduledServicePlanMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServicePlanMetricAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : ScheduledServicePlanMetricRule {
    override fun check(item: ScheduledServicePlanMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServicePlanMetricAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledServicePlanMetricRule {
    override fun check(item: ScheduledServicePlanMetric): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled service plan metric. */
data class ScheduledServicePlanMetricValidator(var rules: MutableList<ScheduledServicePlanMetricRule>) {
    fun validate(item: ScheduledServicePlanMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledServicePlanMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledServicePlanMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledServicePlanMetricValidator(): ScheduledServicePlanMetricValidator {
    return ScheduledServicePlanMetricValidator(
        mutableListOf<ScheduledServicePlanMetricRule>(
            ScheduledServicePlanMetricRequiredCountryRule("scheduled service plan metric needs an entry"),
            ScheduledServicePlanMetricLimitRangeRule(64, 1000),
            ScheduledServicePlanMetricRequiredCodeRule("scheduled service plan metric needs an entry"),
            ScheduledServicePlanMetricTitleLengthRule(1000),
            ScheduledServicePlanMetricAllowedTrailerIdRule(mutableListOf<String>("capped")),
            ScheduledServicePlanMetricAttemptsRangeRule(80, 86400),
        ),
    )
}
