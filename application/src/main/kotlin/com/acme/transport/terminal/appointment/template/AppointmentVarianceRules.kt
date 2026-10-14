package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.text.normalize

/** A rule a appointment variance must pass. Check returns its problems, or none. */
interface AppointmentVarianceRule {
    fun check(item: AppointmentVariance): MutableList<String>
}

data class AppointmentVarianceSegmentLengthRule(var maxLength: Int) : AppointmentVarianceRule {
    override fun check(item: AppointmentVariance): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentVarianceAllowedSegmentRule(var allowed: MutableList<String>) : AppointmentVarianceRule {
    override fun check(item: AppointmentVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentVarianceLocaleLengthRule(var maxLength: Int) : AppointmentVarianceRule {
    override fun check(item: AppointmentVariance): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment variance. */
data class AppointmentVarianceValidator(var rules: MutableList<AppointmentVarianceRule>) {
    fun validate(item: AppointmentVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentVarianceValidator(): AppointmentVarianceValidator {
    return AppointmentVarianceValidator(
        mutableListOf<AppointmentVarianceRule>(
            AppointmentVarianceSegmentLengthRule(32),
            AppointmentVarianceAllowedSegmentRule(mutableListOf<String>("batched", "legacy", "rolling")),
            AppointmentVarianceLocaleLengthRule(120),
        ),
    )
}
