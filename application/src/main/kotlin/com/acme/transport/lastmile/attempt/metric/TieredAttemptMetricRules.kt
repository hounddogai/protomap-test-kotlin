package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.text.normalize

/** A rule a tiered attempt metric must pass. Check returns its problems, or none. */
interface TieredAttemptMetricRule {
    fun check(item: TieredAttemptMetric): MutableList<String>
}

data class TieredAttemptMetricRequiredLabelRule(var message: String) : TieredAttemptMetricRule {
    override fun check(item: TieredAttemptMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAttemptMetricAllowedCodeRule(var allowed: MutableList<String>) : TieredAttemptMetricRule {
    override fun check(item: TieredAttemptMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TieredAttemptMetricLabelLengthRule(var maxLength: Int) : TieredAttemptMetricRule {
    override fun check(item: TieredAttemptMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAttemptMetricRequiredDockRule(var message: String) : TieredAttemptMetricRule {
    override fun check(item: TieredAttemptMetric): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAttemptMetricRequiredCountryRule(var message: String) : TieredAttemptMetricRule {
    override fun check(item: TieredAttemptMetric): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered attempt metric. */
data class TieredAttemptMetricValidator(var rules: MutableList<TieredAttemptMetricRule>) {
    fun validate(item: TieredAttemptMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredAttemptMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAttemptMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAttemptMetricValidator(): TieredAttemptMetricValidator {
    return TieredAttemptMetricValidator(
        mutableListOf<TieredAttemptMetricRule>(
            TieredAttemptMetricRequiredLabelRule("tiered attempt metric needs a value"),
            TieredAttemptMetricAllowedCodeRule(mutableListOf<String>("lenient")),
            TieredAttemptMetricLabelLengthRule(32),
            TieredAttemptMetricRequiredDockRule("tiered attempt metric needs a value"),
            TieredAttemptMetricRequiredCountryRule("tiered attempt metric needs this field"),
        ),
    )
}
