package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.kit.text.normalize

/** A rule a appointment schedule must pass. Check returns its problems, or none. */
interface AppointmentScheduleRule {
    fun check(item: AppointmentSchedule): MutableList<String>
}

data class AppointmentScheduleTrailerIdLengthRule(var maxLength: Int) : AppointmentScheduleRule {
    override fun check(item: AppointmentSchedule): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentScheduleRequiredTrailerIdRule(var message: String) : AppointmentScheduleRule {
    override fun check(item: AppointmentSchedule): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentScheduleAllowedSegmentRule(var allowed: MutableList<String>) : AppointmentScheduleRule {
    override fun check(item: AppointmentSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment schedule. */
data class AppointmentScheduleValidator(var rules: MutableList<AppointmentScheduleRule>) {
    fun validate(item: AppointmentSchedule): MutableList<String> {
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

    fun requireValid(item: AppointmentSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentScheduleValidator(): AppointmentScheduleValidator {
    return AppointmentScheduleValidator(
        mutableListOf<AppointmentScheduleRule>(
            AppointmentScheduleTrailerIdLengthRule(16),
            AppointmentScheduleRequiredTrailerIdRule("appointment schedule needs an entry"),
            AppointmentScheduleAllowedSegmentRule(mutableListOf<String>("rolling", "global", "adaptive")),
        ),
    )
}
