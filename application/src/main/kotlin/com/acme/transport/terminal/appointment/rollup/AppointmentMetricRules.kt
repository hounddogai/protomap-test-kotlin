package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.text.normalize

/** A rule a appointment metric must pass. Check returns its problems, or none. */
interface AppointmentMetricRule {
    fun check(item: AppointmentMetric): MutableList<String>
}

data class AppointmentMetricCurrencyLengthRule(var maxLength: Int) : AppointmentMetricRule {
    override fun check(item: AppointmentMetric): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentMetricCodePrefixRule(var prefix: String) : AppointmentMetricRule {
    override fun check(item: AppointmentMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentMetricAllowedNotesRule(var allowed: MutableList<String>) : AppointmentMetricRule {
    override fun check(item: AppointmentMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment metric. */
data class AppointmentMetricValidator(var rules: MutableList<AppointmentMetricRule>) {
    fun validate(item: AppointmentMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentMetricValidator(): AppointmentMetricValidator {
    return AppointmentMetricValidator(
        mutableListOf<AppointmentMetricRule>(
            AppointmentMetricCurrencyLengthRule(255),
            AppointmentMetricCodePrefixRule("X"),
            AppointmentMetricAllowedNotesRule(mutableListOf<String>("capped", "regional", "weighted")),
        ),
    )
}
