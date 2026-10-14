package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.text.normalize

/** A rule a drop schedule must pass. Check returns its problems, or none. */
interface DropScheduleRule {
    fun check(item: DropSchedule): MutableList<String>
}

data class DropScheduleNotesLengthRule(var maxLength: Int) : DropScheduleRule {
    override fun check(item: DropSchedule): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropScheduleAllowedChannelRule(var allowed: MutableList<String>) : DropScheduleRule {
    override fun check(item: DropSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is expired")
        }
        return mutableListOf<String>()
    }
}

data class DropScheduleChannelLengthRule(var maxLength: Int) : DropScheduleRule {
    override fun check(item: DropSchedule): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropScheduleRequiredChannelRule(var message: String) : DropScheduleRule {
    override fun check(item: DropSchedule): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropScheduleWindowDaysRangeRule(var min: Int, var max: Int) : DropScheduleRule {
    override fun check(item: DropSchedule): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class DropScheduleRequiredNotesRule(var message: String) : DropScheduleRule {
    override fun check(item: DropSchedule): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop schedule. */
data class DropScheduleValidator(var rules: MutableList<DropScheduleRule>) {
    fun validate(item: DropSchedule): MutableList<String> {
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

    fun requireValid(item: DropSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDropScheduleValidator(): DropScheduleValidator {
    return DropScheduleValidator(
        mutableListOf<DropScheduleRule>(
            DropScheduleNotesLengthRule(16),
            DropScheduleAllowedChannelRule(mutableListOf<String>("default", "rolling", "pending")),
            DropScheduleChannelLengthRule(64),
            DropScheduleRequiredChannelRule("drop schedule needs a value"),
            DropScheduleWindowDaysRangeRule(16, 255),
            DropScheduleRequiredNotesRule("drop schedule needs an entry"),
        ),
    )
}
