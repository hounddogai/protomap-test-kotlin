package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.normalize

/** A rule a scheduled near miss schedule must pass. Check returns its problems, or none. */
interface ScheduledNearMissScheduleRule {
    fun check(item: ScheduledNearMissSchedule): MutableList<String>
}

data class ScheduledNearMissScheduleCodeLengthRule(var maxLength: Int) : ScheduledNearMissScheduleRule {
    override fun check(item: ScheduledNearMissSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissScheduleLabelLengthRule(var maxLength: Int) : ScheduledNearMissScheduleRule {
    override fun check(item: ScheduledNearMissSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissScheduleAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledNearMissScheduleRule {
    override fun check(item: ScheduledNearMissSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissScheduleCodePrefixRule(var prefix: String) : ScheduledNearMissScheduleRule {
    override fun check(item: ScheduledNearMissSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled near miss schedule. */
data class ScheduledNearMissScheduleValidator(var rules: MutableList<ScheduledNearMissScheduleRule>) {
    fun validate(item: ScheduledNearMissSchedule): MutableList<String> {
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

    fun requireValid(item: ScheduledNearMissSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledNearMissSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledNearMissScheduleValidator(): ScheduledNearMissScheduleValidator {
    return ScheduledNearMissScheduleValidator(
        mutableListOf<ScheduledNearMissScheduleRule>(
            ScheduledNearMissScheduleCodeLengthRule(3600),
            ScheduledNearMissScheduleLabelLengthRule(80),
            ScheduledNearMissScheduleAllowedCodeRule(mutableListOf<String>()),
            ScheduledNearMissScheduleCodePrefixRule("OPS"),
        ),
    )
}
