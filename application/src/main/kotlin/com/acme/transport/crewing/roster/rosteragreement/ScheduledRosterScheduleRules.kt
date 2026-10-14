package com.acme.transport.crewing.roster.rosteragreement

/** A rule a scheduled roster schedule must pass. Check returns its problems, or none. */
interface ScheduledRosterScheduleRule {
    fun check(item: ScheduledRosterSchedule): MutableList<String>
}

data class ScheduledRosterScheduleUnitLengthRule(var maxLength: Int) : ScheduledRosterScheduleRule {
    override fun check(item: ScheduledRosterSchedule): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterScheduleMinimumRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledRosterScheduleRule {
    override fun check(item: ScheduledRosterSchedule): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class ScheduledRosterScheduleRequiredCodeRule(var message: String) : ScheduledRosterScheduleRule {
    override fun check(item: ScheduledRosterSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterScheduleLabelLengthRule(var maxLength: Int) : ScheduledRosterScheduleRule {
    override fun check(item: ScheduledRosterSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled roster schedule. */
data class ScheduledRosterScheduleValidator(var rules: MutableList<ScheduledRosterScheduleRule>) {
    fun validate(item: ScheduledRosterSchedule): MutableList<String> {
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

    fun requireValid(item: ScheduledRosterSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRosterSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRosterScheduleValidator(): ScheduledRosterScheduleValidator {
    return ScheduledRosterScheduleValidator(
        mutableListOf<ScheduledRosterScheduleRule>(
            ScheduledRosterScheduleUnitLengthRule(3600),
            ScheduledRosterScheduleMinimumRangeRule(4.9, 728.5),
            ScheduledRosterScheduleRequiredCodeRule("scheduled roster schedule needs a value"),
            ScheduledRosterScheduleLabelLengthRule(255),
        ),
    )
}
