package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.text.normalize

/** A rule a scheduled utilization milestone must pass. Check returns its problems, or none. */
interface ScheduledUtilizationMilestoneRule {
    fun check(item: ScheduledUtilizationMilestone): MutableList<String>
}

data class ScheduledUtilizationMilestoneAllowedCategoryRule(
    var allowed: MutableList<String>,
) : ScheduledUtilizationMilestoneRule {
    override fun check(item: ScheduledUtilizationMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledUtilizationMilestoneRequiredTrailerIdRule(
    var message: String,
) : ScheduledUtilizationMilestoneRule {
    override fun check(item: ScheduledUtilizationMilestone): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledUtilizationMilestoneCategoryLengthRule(
    var maxLength: Int,
) : ScheduledUtilizationMilestoneRule {
    override fun check(item: ScheduledUtilizationMilestone): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledUtilizationMilestoneEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledUtilizationMilestoneRule {
    override fun check(item: ScheduledUtilizationMilestone): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledUtilizationMilestoneAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledUtilizationMilestoneRule {
    override fun check(item: ScheduledUtilizationMilestone): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class ScheduledUtilizationMilestoneCodePrefixRule(var prefix: String) : ScheduledUtilizationMilestoneRule {
    override fun check(item: ScheduledUtilizationMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled utilization milestone. */
data class ScheduledUtilizationMilestoneValidator(var rules: MutableList<ScheduledUtilizationMilestoneRule>) {
    fun validate(item: ScheduledUtilizationMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledUtilizationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledUtilizationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledUtilizationMilestoneValidator(): ScheduledUtilizationMilestoneValidator {
    return ScheduledUtilizationMilestoneValidator(
        mutableListOf<ScheduledUtilizationMilestoneRule>(
            ScheduledUtilizationMilestoneAllowedCategoryRule(mutableListOf<String>("capped")),
            ScheduledUtilizationMilestoneRequiredTrailerIdRule(
                "scheduled utilization milestone needs this field",
            ),
            ScheduledUtilizationMilestoneCategoryLengthRule(16),
            ScheduledUtilizationMilestoneEndsAtRangeRule(16, 80),
            ScheduledUtilizationMilestoneAttemptsRangeRule(255, 86400),
            ScheduledUtilizationMilestoneCodePrefixRule("ACM"),
        ),
    )
}
