package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.text.normalize

/** A rule a weighted appointment metric must pass. Check returns its problems, or none. */
interface WeightedAppointmentMetricRule {
    fun check(item: WeightedAppointmentMetric): MutableList<String>
}

data class WeightedAppointmentMetricExternalRefLengthRule(var maxLength: Int) : WeightedAppointmentMetricRule {
    override fun check(item: WeightedAppointmentMetric): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAppointmentMetricRequiredExternalRefRule(
    var message: String,
) : WeightedAppointmentMetricRule {
    override fun check(item: WeightedAppointmentMetric): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAppointmentMetricAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : WeightedAppointmentMetricRule {
    override fun check(item: WeightedAppointmentMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted appointment metric. */
data class WeightedAppointmentMetricValidator(var rules: MutableList<WeightedAppointmentMetricRule>) {
    fun validate(item: WeightedAppointmentMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAppointmentMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAppointmentMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAppointmentMetricValidator(): WeightedAppointmentMetricValidator {
    return WeightedAppointmentMetricValidator(
        mutableListOf<WeightedAppointmentMetricRule>(
            WeightedAppointmentMetricExternalRefLengthRule(64),
            WeightedAppointmentMetricRequiredExternalRefRule("weighted appointment metric needs an entry"),
            WeightedAppointmentMetricAllowedExternalRefRule(mutableListOf<String>("strict", "strict")),
        ),
    )
}
