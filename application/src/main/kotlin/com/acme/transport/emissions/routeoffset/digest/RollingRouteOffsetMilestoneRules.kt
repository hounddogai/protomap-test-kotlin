package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.kit.text.normalize

/** A rule a rolling route offset milestone must pass. Check returns its problems, or none. */
interface RollingRouteOffsetMilestoneRule {
    fun check(item: RollingRouteOffsetMilestone): MutableList<String>
}

data class RollingRouteOffsetMilestoneStopCountRangeRule(
    var min: Int,
    var max: Int,
) : RollingRouteOffsetMilestoneRule {
    override fun check(item: RollingRouteOffsetMilestone): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingRouteOffsetMilestoneAllowedLabelRule(
    var allowed: MutableList<String>,
) : RollingRouteOffsetMilestoneRule {
    override fun check(item: RollingRouteOffsetMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RollingRouteOffsetMilestoneRequiredLabelRule(var message: String) : RollingRouteOffsetMilestoneRule {
    override fun check(item: RollingRouteOffsetMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRouteOffsetMilestoneLabelLengthRule(var maxLength: Int) : RollingRouteOffsetMilestoneRule {
    override fun check(item: RollingRouteOffsetMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRouteOffsetMilestoneCodePrefixRule(var prefix: String) : RollingRouteOffsetMilestoneRule {
    override fun check(item: RollingRouteOffsetMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling route offset milestone. */
data class RollingRouteOffsetMilestoneValidator(var rules: MutableList<RollingRouteOffsetMilestoneRule>) {
    fun validate(item: RollingRouteOffsetMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingRouteOffsetMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRouteOffsetMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRouteOffsetMilestoneValidator(): RollingRouteOffsetMilestoneValidator {
    return RollingRouteOffsetMilestoneValidator(
        mutableListOf<RollingRouteOffsetMilestoneRule>(
            RollingRouteOffsetMilestoneStopCountRangeRule(32, 64),
            RollingRouteOffsetMilestoneAllowedLabelRule(mutableListOf<String>("regional", "global")),
            RollingRouteOffsetMilestoneRequiredLabelRule("rolling route offset milestone needs a value"),
            RollingRouteOffsetMilestoneLabelLengthRule(16),
            RollingRouteOffsetMilestoneCodePrefixRule("Q"),
        ),
    )
}
