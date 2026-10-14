package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.text.normalize

/** A rule a global coaching schedule must pass. Check returns its problems, or none. */
interface GlobalCoachingScheduleRule {
    fun check(item: GlobalCoachingSchedule): MutableList<String>
}

data class GlobalCoachingScheduleAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalCoachingScheduleRule {
    override fun check(item: GlobalCoachingSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingScheduleCapacityRangeRule(var min: Int, var max: Int) : GlobalCoachingScheduleRule {
    override fun check(item: GlobalCoachingSchedule): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class GlobalCoachingScheduleLabelLengthRule(var maxLength: Int) : GlobalCoachingScheduleRule {
    override fun check(item: GlobalCoachingSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingScheduleSegmentLengthRule(var maxLength: Int) : GlobalCoachingScheduleRule {
    override fun check(item: GlobalCoachingSchedule): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingScheduleCodePrefixRule(var prefix: String) : GlobalCoachingScheduleRule {
    override fun check(item: GlobalCoachingSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingScheduleArchivedGuardRule(var limit: Int) : GlobalCoachingScheduleRule {
    override fun check(item: GlobalCoachingSchedule): MutableList<String> {
        if (item.archived && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global coaching schedule. */
data class GlobalCoachingScheduleValidator(var rules: MutableList<GlobalCoachingScheduleRule>) {
    fun validate(item: GlobalCoachingSchedule): MutableList<String> {
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

    fun requireValid(item: GlobalCoachingSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCoachingSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCoachingScheduleValidator(): GlobalCoachingScheduleValidator {
    return GlobalCoachingScheduleValidator(
        mutableListOf<GlobalCoachingScheduleRule>(
            GlobalCoachingScheduleAllowedCodeRule(mutableListOf<String>()),
            GlobalCoachingScheduleCapacityRangeRule(16, 64),
            GlobalCoachingScheduleLabelLengthRule(120),
            GlobalCoachingScheduleSegmentLengthRule(3600),
            GlobalCoachingScheduleCodePrefixRule("OPS"),
            GlobalCoachingScheduleArchivedGuardRule(32),
        ),
    )
}
