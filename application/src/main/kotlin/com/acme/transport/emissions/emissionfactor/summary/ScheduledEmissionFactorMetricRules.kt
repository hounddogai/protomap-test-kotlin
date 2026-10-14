package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.text.normalize

/** A rule a scheduled emission factor metric must pass. Check returns its problems, or none. */
interface ScheduledEmissionFactorMetricRule {
    fun check(item: ScheduledEmissionFactorMetric): MutableList<String>
}

data class ScheduledEmissionFactorMetricRequiredCodeRule(
    var message: String,
) : ScheduledEmissionFactorMetricRule {
    override fun check(item: ScheduledEmissionFactorMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEmissionFactorMetricRequiredCarrierRule(
    var message: String,
) : ScheduledEmissionFactorMetricRule {
    override fun check(item: ScheduledEmissionFactorMetric): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEmissionFactorMetricAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledEmissionFactorMetricRule {
    override fun check(item: ScheduledEmissionFactorMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled emission factor metric. */
data class ScheduledEmissionFactorMetricValidator(var rules: MutableList<ScheduledEmissionFactorMetricRule>) {
    fun validate(item: ScheduledEmissionFactorMetric): MutableList<String> {
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

    fun requireValid(item: ScheduledEmissionFactorMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledEmissionFactorMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledEmissionFactorMetricValidator(): ScheduledEmissionFactorMetricValidator {
    return ScheduledEmissionFactorMetricValidator(
        mutableListOf<ScheduledEmissionFactorMetricRule>(
            ScheduledEmissionFactorMetricRequiredCodeRule("scheduled emission factor metric needs an entry"),
            ScheduledEmissionFactorMetricRequiredCarrierRule(
                "scheduled emission factor metric needs this field",
            ),
            ScheduledEmissionFactorMetricAllowedCodeRule(mutableListOf<String>("pending", "fallback")),
        ),
    )
}
