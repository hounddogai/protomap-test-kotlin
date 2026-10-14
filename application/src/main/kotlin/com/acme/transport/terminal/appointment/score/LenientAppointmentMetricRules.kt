package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.text.normalize

/** A rule a lenient appointment metric must pass. Check returns its problems, or none. */
interface LenientAppointmentMetricRule {
    fun check(item: LenientAppointmentMetric): MutableList<String>
}

data class LenientAppointmentMetricCodePrefixRule(var prefix: String) : LenientAppointmentMetricRule {
    override fun check(item: LenientAppointmentMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentMetricRequiredLabelRule(var message: String) : LenientAppointmentMetricRule {
    override fun check(item: LenientAppointmentMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentMetricAllowedLabelRule(
    var allowed: MutableList<String>,
) : LenientAppointmentMetricRule {
    override fun check(item: LenientAppointmentMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentMetricBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : LenientAppointmentMetricRule {
    override fun check(item: LenientAppointmentMetric): MutableList<String> {
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

data class LenientAppointmentMetricRequiredCodeRule(var message: String) : LenientAppointmentMetricRule {
    override fun check(item: LenientAppointmentMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentMetricSequenceRangeRule(
    var min: Int,
    var max: Int,
) : LenientAppointmentMetricRule {
    override fun check(item: LenientAppointmentMetric): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient appointment metric. */
data class LenientAppointmentMetricValidator(var rules: MutableList<LenientAppointmentMetricRule>) {
    fun validate(item: LenientAppointmentMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientAppointmentMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAppointmentMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAppointmentMetricValidator(): LenientAppointmentMetricValidator {
    return LenientAppointmentMetricValidator(
        mutableListOf<LenientAppointmentMetricRule>(
            LenientAppointmentMetricCodePrefixRule("Q"),
            LenientAppointmentMetricRequiredLabelRule("lenient appointment metric needs a value"),
            LenientAppointmentMetricAllowedLabelRule(mutableListOf<String>("scheduled", "manual")),
            LenientAppointmentMetricBatchSizeRangeRule(32, 255),
            LenientAppointmentMetricRequiredCodeRule("lenient appointment metric needs this field"),
            LenientAppointmentMetricSequenceRangeRule(32, 64),
        ),
    )
}
