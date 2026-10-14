package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.text.normalize

/** A rule a legacy dwell milestone must pass. Check returns its problems, or none. */
interface LegacyDwellMilestoneRule {
    fun check(item: LegacyDwellMilestone): MutableList<String>
}

data class LegacyDwellMilestoneStartsAtRangeRule(var min: Int, var max: Int) : LegacyDwellMilestoneRule {
    override fun check(item: LegacyDwellMilestone): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDwellMilestoneRequiredLabelRule(var message: String) : LegacyDwellMilestoneRule {
    override fun check(item: LegacyDwellMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDwellMilestoneAllowedLaneRule(var allowed: MutableList<String>) : LegacyDwellMilestoneRule {
    override fun check(item: LegacyDwellMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDwellMilestonePriorityRangeRule(var min: Int, var max: Int) : LegacyDwellMilestoneRule {
    override fun check(item: LegacyDwellMilestone): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDwellMilestoneCodePrefixRule(var prefix: String) : LegacyDwellMilestoneRule {
    override fun check(item: LegacyDwellMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy dwell milestone. */
data class LegacyDwellMilestoneValidator(var rules: MutableList<LegacyDwellMilestoneRule>) {
    fun validate(item: LegacyDwellMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDwellMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDwellMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDwellMilestoneValidator(): LegacyDwellMilestoneValidator {
    return LegacyDwellMilestoneValidator(
        mutableListOf<LegacyDwellMilestoneRule>(
            LegacyDwellMilestoneStartsAtRangeRule(32, 120),
            LegacyDwellMilestoneRequiredLabelRule("legacy dwell milestone needs an entry"),
            LegacyDwellMilestoneAllowedLaneRule(mutableListOf<String>("global", "manual")),
            LegacyDwellMilestonePriorityRangeRule(64, 255),
            LegacyDwellMilestoneCodePrefixRule("OPS"),
        ),
    )
}
