package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a regional appointment variance must pass. Check returns its problems, or none. */
interface RegionalAppointmentVarianceRule {
    fun check(item: RegionalAppointmentVariance): MutableList<String>
}

data class RegionalAppointmentVarianceLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : RegionalAppointmentVarianceRule {
    override fun check(item: RegionalAppointmentVariance): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class RegionalAppointmentVarianceAllowedLocaleRule(
    var allowed: MutableList<String>,
) : RegionalAppointmentVarianceRule {
    override fun check(item: RegionalAppointmentVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAppointmentVarianceCostRangeRule(
    var min: Double,
    var max: Double,
) : RegionalAppointmentVarianceRule {
    override fun check(item: RegionalAppointmentVariance): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAppointmentVarianceRequiredNameRule(var message: String) : RegionalAppointmentVarianceRule {
    override fun check(item: RegionalAppointmentVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAppointmentVarianceCarrierLengthRule(var maxLength: Int) : RegionalAppointmentVarianceRule {
    override fun check(item: RegionalAppointmentVariance): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional appointment variance. */
data class RegionalAppointmentVarianceValidator(var rules: MutableList<RegionalAppointmentVarianceRule>) {
    fun validate(item: RegionalAppointmentVariance): MutableList<String> {
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

    fun requireValid(item: RegionalAppointmentVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAppointmentVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAppointmentVarianceValidator(): RegionalAppointmentVarianceValidator {
    return RegionalAppointmentVarianceValidator(
        mutableListOf<RegionalAppointmentVarianceRule>(
            RegionalAppointmentVarianceLoadKgRangeRule(5.0, 4444.9),
            RegionalAppointmentVarianceAllowedLocaleRule(mutableListOf<String>()),
            RegionalAppointmentVarianceCostRangeRule(4.4, 4264.8),
            RegionalAppointmentVarianceRequiredNameRule("regional appointment variance needs this field"),
            RegionalAppointmentVarianceCarrierLengthRule(255),
        ),
    )
}
