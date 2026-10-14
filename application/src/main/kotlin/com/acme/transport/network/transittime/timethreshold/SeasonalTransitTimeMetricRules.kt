package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.text.normalize

/** A rule a seasonal transit time metric must pass. Check returns its problems, or none. */
interface SeasonalTransitTimeMetricRule {
    fun check(item: SeasonalTransitTimeMetric): MutableList<String>
}

data class SeasonalTransitTimeMetricRequiredLocaleRule(var message: String) : SeasonalTransitTimeMetricRule {
    override fun check(item: SeasonalTransitTimeMetric): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeMetricAllowedLocaleRule(
    var allowed: MutableList<String>,
) : SeasonalTransitTimeMetricRule {
    override fun check(item: SeasonalTransitTimeMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is too long")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeMetricRequiredTitleRule(var message: String) : SeasonalTransitTimeMetricRule {
    override fun check(item: SeasonalTransitTimeMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeMetricAmountRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalTransitTimeMetricRule {
    override fun check(item: SeasonalTransitTimeMetric): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class SeasonalTransitTimeMetricEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalTransitTimeMetricRule {
    override fun check(item: SeasonalTransitTimeMetric): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class SeasonalTransitTimeMetricTitleLengthRule(var maxLength: Int) : SeasonalTransitTimeMetricRule {
    override fun check(item: SeasonalTransitTimeMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal transit time metric. */
data class SeasonalTransitTimeMetricValidator(var rules: MutableList<SeasonalTransitTimeMetricRule>) {
    fun validate(item: SeasonalTransitTimeMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTransitTimeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTransitTimeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTransitTimeMetricValidator(): SeasonalTransitTimeMetricValidator {
    return SeasonalTransitTimeMetricValidator(
        mutableListOf<SeasonalTransitTimeMetricRule>(
            SeasonalTransitTimeMetricRequiredLocaleRule("seasonal transit time metric needs an entry"),
            SeasonalTransitTimeMetricAllowedLocaleRule(mutableListOf<String>("primary", "global", "fallback")),
            SeasonalTransitTimeMetricRequiredTitleRule("seasonal transit time metric needs an entry"),
            SeasonalTransitTimeMetricAmountRangeRule(1.3, 58.5),
            SeasonalTransitTimeMetricEndsAtRangeRule(64, 3600),
            SeasonalTransitTimeMetricTitleLengthRule(3600),
        ),
    )
}
