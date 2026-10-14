package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.text.normalize

/** A rule a scheduled roster cycle must pass. Check returns its problems, or none. */
interface ScheduledRosterCycleRule {
    fun check(item: ScheduledRosterCycle): MutableList<String>
}

data class ScheduledRosterCycleAllowedTitleRule(var allowed: MutableList<String>) : ScheduledRosterCycleRule {
    override fun check(item: ScheduledRosterCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterCycleTitleLengthRule(var maxLength: Int) : ScheduledRosterCycleRule {
    override fun check(item: ScheduledRosterCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterCycleSequenceRangeRule(var min: Int, var max: Int) : ScheduledRosterCycleRule {
    override fun check(item: ScheduledRosterCycle): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled roster cycle. */
data class ScheduledRosterCycleValidator(var rules: MutableList<ScheduledRosterCycleRule>) {
    fun validate(item: ScheduledRosterCycle): MutableList<String> {
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

    fun requireValid(item: ScheduledRosterCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRosterCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRosterCycleValidator(): ScheduledRosterCycleValidator {
    return ScheduledRosterCycleValidator(
        mutableListOf<ScheduledRosterCycleRule>(
            ScheduledRosterCycleAllowedTitleRule(mutableListOf<String>("strict", "manual", "default")),
            ScheduledRosterCycleTitleLengthRule(1000),
            ScheduledRosterCycleSequenceRangeRule(64, 64),
        ),
    )
}
