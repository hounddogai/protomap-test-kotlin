package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.text.normalize

/** A rule a utilization milestone must pass. Check returns its problems, or none. */
interface UtilizationMilestoneRule {
    fun check(item: UtilizationMilestone): MutableList<String>
}

data class UtilizationMilestoneAllowedLabelRule(var allowed: MutableList<String>) : UtilizationMilestoneRule {
    override fun check(item: UtilizationMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationMilestoneCodeLengthRule(var maxLength: Int) : UtilizationMilestoneRule {
    override fun check(item: UtilizationMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationMilestoneAmountRangeRule(var min: Double, var max: Double) : UtilizationMilestoneRule {
    override fun check(item: UtilizationMilestone): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class UtilizationMilestoneRequiredSegmentRule(var message: String) : UtilizationMilestoneRule {
    override fun check(item: UtilizationMilestone): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationMilestoneLabelLengthRule(var maxLength: Int) : UtilizationMilestoneRule {
    override fun check(item: UtilizationMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationMilestoneRequiredCodeRule(var message: String) : UtilizationMilestoneRule {
    override fun check(item: UtilizationMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization milestone. */
data class UtilizationMilestoneValidator(var rules: MutableList<UtilizationMilestoneRule>) {
    fun validate(item: UtilizationMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationMilestoneValidator(): UtilizationMilestoneValidator {
    return UtilizationMilestoneValidator(
        mutableListOf<UtilizationMilestoneRule>(
            UtilizationMilestoneAllowedLabelRule(mutableListOf<String>()),
            UtilizationMilestoneCodeLengthRule(1000),
            UtilizationMilestoneAmountRangeRule(3.1, 1142.1),
            UtilizationMilestoneRequiredSegmentRule("utilization milestone needs an entry"),
            UtilizationMilestoneLabelLengthRule(64),
            UtilizationMilestoneRequiredCodeRule("utilization milestone needs a value"),
        ),
    )
}
