package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.text.normalize

/** A rule a scheduled appointment variance must pass. Check returns its problems, or none. */
interface ScheduledAppointmentVarianceRule {
    fun check(item: ScheduledAppointmentVariance): MutableList<String>
}

data class ScheduledAppointmentVarianceUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledAppointmentVarianceRule {
    override fun check(item: ScheduledAppointmentVariance): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class ScheduledAppointmentVarianceRateRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledAppointmentVarianceRule {
    override fun check(item: ScheduledAppointmentVariance): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class ScheduledAppointmentVarianceAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledAppointmentVarianceRule {
    override fun check(item: ScheduledAppointmentVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentVarianceCodeLengthRule(var maxLength: Int) : ScheduledAppointmentVarianceRule {
    override fun check(item: ScheduledAppointmentVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentVarianceTitleLengthRule(var maxLength: Int) : ScheduledAppointmentVarianceRule {
    override fun check(item: ScheduledAppointmentVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled appointment variance. */
data class ScheduledAppointmentVarianceValidator(var rules: MutableList<ScheduledAppointmentVarianceRule>) {
    fun validate(item: ScheduledAppointmentVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAppointmentVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAppointmentVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAppointmentVarianceValidator(): ScheduledAppointmentVarianceValidator {
    return ScheduledAppointmentVarianceValidator(
        mutableListOf<ScheduledAppointmentVarianceRule>(
            ScheduledAppointmentVarianceUpdatedAtRangeRule(64, 86400),
            ScheduledAppointmentVarianceRateRangeRule(8.0, 3766.8),
            ScheduledAppointmentVarianceAllowedCodeRule(mutableListOf<String>("lenient")),
            ScheduledAppointmentVarianceCodeLengthRule(86400),
            ScheduledAppointmentVarianceTitleLengthRule(255),
        ),
    )
}
