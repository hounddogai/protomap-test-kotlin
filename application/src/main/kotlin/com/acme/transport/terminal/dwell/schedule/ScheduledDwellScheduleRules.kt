package com.acme.transport.terminal.dwell.schedule

import com.acme.transport.kit.text.normalize

/** A rule a scheduled dwell schedule must pass. Check returns its problems, or none. */
interface ScheduledDwellScheduleRule {
    fun check(item: ScheduledDwellSchedule): MutableList<String>
}

data class ScheduledDwellScheduleNameLengthRule(var maxLength: Int) : ScheduledDwellScheduleRule {
    override fun check(item: ScheduledDwellSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellScheduleRequiredCodeRule(var message: String) : ScheduledDwellScheduleRule {
    override fun check(item: ScheduledDwellSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellScheduleAllowedDockRule(
    var allowed: MutableList<String>,
) : ScheduledDwellScheduleRule {
    override fun check(item: ScheduledDwellSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dwell schedule. */
data class ScheduledDwellScheduleValidator(var rules: MutableList<ScheduledDwellScheduleRule>) {
    fun validate(item: ScheduledDwellSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDwellSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDwellSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDwellScheduleValidator(): ScheduledDwellScheduleValidator {
    return ScheduledDwellScheduleValidator(
        mutableListOf<ScheduledDwellScheduleRule>(
            ScheduledDwellScheduleNameLengthRule(255),
            ScheduledDwellScheduleRequiredCodeRule("scheduled dwell schedule needs this field"),
            ScheduledDwellScheduleAllowedDockRule(mutableListOf<String>("manual", "strict")),
        ),
    )
}
