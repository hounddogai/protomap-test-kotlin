package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a weighted utilization milestone must pass. Check returns its problems, or none. */
interface WeightedUtilizationMilestoneRule {
    fun check(item: WeightedUtilizationMilestone): MutableList<String>
}

data class WeightedUtilizationMilestoneContactEmailLengthRule(
    var maxLength: Int,
) : WeightedUtilizationMilestoneRule {
    override fun check(item: WeightedUtilizationMilestone): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationMilestoneTitleLengthRule(var maxLength: Int) : WeightedUtilizationMilestoneRule {
    override fun check(item: WeightedUtilizationMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationMilestoneAllowedTitleRule(
    var allowed: MutableList<String>,
) : WeightedUtilizationMilestoneRule {
    override fun check(item: WeightedUtilizationMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted utilization milestone. */
data class WeightedUtilizationMilestoneValidator(var rules: MutableList<WeightedUtilizationMilestoneRule>) {
    fun validate(item: WeightedUtilizationMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedUtilizationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedUtilizationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedUtilizationMilestoneValidator(): WeightedUtilizationMilestoneValidator {
    return WeightedUtilizationMilestoneValidator(
        mutableListOf<WeightedUtilizationMilestoneRule>(
            WeightedUtilizationMilestoneContactEmailLengthRule(80),
            WeightedUtilizationMilestoneTitleLengthRule(32),
            WeightedUtilizationMilestoneAllowedTitleRule(
                mutableListOf<String>("scheduled", "rolling", "batched"),
            ),
        ),
    )
}
