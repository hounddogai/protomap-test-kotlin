package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.text.normalize

/** A rule a scheduled accessorial schedule must pass. Check returns its problems, or none. */
interface ScheduledAccessorialScheduleRule {
    fun check(item: ScheduledAccessorialSchedule): MutableList<String>
}

data class ScheduledAccessorialScheduleCodePrefixRule(var prefix: String) : ScheduledAccessorialScheduleRule {
    override fun check(item: ScheduledAccessorialSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialScheduleCodeLengthRule(var maxLength: Int) : ScheduledAccessorialScheduleRule {
    override fun check(item: ScheduledAccessorialSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialScheduleAllowedDockRule(
    var allowed: MutableList<String>,
) : ScheduledAccessorialScheduleRule {
    override fun check(item: ScheduledAccessorialSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialScheduleDockLengthRule(var maxLength: Int) : ScheduledAccessorialScheduleRule {
    override fun check(item: ScheduledAccessorialSchedule): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialScheduleFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledAccessorialScheduleRule {
    override fun check(item: ScheduledAccessorialSchedule): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class ScheduledAccessorialScheduleLaneLengthRule(var maxLength: Int) : ScheduledAccessorialScheduleRule {
    override fun check(item: ScheduledAccessorialSchedule): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled accessorial schedule. */
data class ScheduledAccessorialScheduleValidator(var rules: MutableList<ScheduledAccessorialScheduleRule>) {
    fun validate(item: ScheduledAccessorialSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAccessorialSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAccessorialSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAccessorialScheduleValidator(): ScheduledAccessorialScheduleValidator {
    return ScheduledAccessorialScheduleValidator(
        mutableListOf<ScheduledAccessorialScheduleRule>(
            ScheduledAccessorialScheduleCodePrefixRule("REF"),
            ScheduledAccessorialScheduleCodeLengthRule(120),
            ScheduledAccessorialScheduleAllowedDockRule(
                mutableListOf<String>("regional", "capped", "scheduled"),
            ),
            ScheduledAccessorialScheduleDockLengthRule(3600),
            ScheduledAccessorialScheduleFloorValueRangeRule(3.8, 1611.1),
            ScheduledAccessorialScheduleLaneLengthRule(32),
        ),
    )
}
