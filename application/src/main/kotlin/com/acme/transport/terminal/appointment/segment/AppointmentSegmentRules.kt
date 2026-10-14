package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.text.normalize

/** A rule a appointment segment must pass. Check returns its problems, or none. */
interface AppointmentSegmentRule {
    fun check(item: AppointmentSegment): MutableList<String>
}

data class AppointmentSegmentAllowedCarrierRule(var allowed: MutableList<String>) : AppointmentSegmentRule {
    override fun check(item: AppointmentSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentSegmentDurationMinutesRangeRule(var min: Int, var max: Int) : AppointmentSegmentRule {
    override fun check(item: AppointmentSegment): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentSegmentUnitLengthRule(var maxLength: Int) : AppointmentSegmentRule {
    override fun check(item: AppointmentSegment): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment segment. */
data class AppointmentSegmentValidator(var rules: MutableList<AppointmentSegmentRule>) {
    fun validate(item: AppointmentSegment): MutableList<String> {
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

    fun requireValid(item: AppointmentSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentSegmentValidator(): AppointmentSegmentValidator {
    return AppointmentSegmentValidator(
        mutableListOf<AppointmentSegmentRule>(
            AppointmentSegmentAllowedCarrierRule(mutableListOf<String>()),
            AppointmentSegmentDurationMinutesRangeRule(1000, 1000),
            AppointmentSegmentUnitLengthRule(64),
        ),
    )
}
