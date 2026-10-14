package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.kit.text.normalize

/** A rule a dispute milestone must pass. Check returns its problems, or none. */
interface DisputeMilestoneRule {
    fun check(item: DisputeMilestone): MutableList<String>
}

data class DisputeMilestoneRequiredCodeRule(var message: String) : DisputeMilestoneRule {
    override fun check(item: DisputeMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeMilestoneLabelLengthRule(var maxLength: Int) : DisputeMilestoneRule {
    override fun check(item: DisputeMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeMilestoneUpdatedAtRangeRule(var min: Int, var max: Int) : DisputeMilestoneRule {
    override fun check(item: DisputeMilestone): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class DisputeMilestoneLaneLengthRule(var maxLength: Int) : DisputeMilestoneRule {
    override fun check(item: DisputeMilestone): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeMilestoneRequiredLabelRule(var message: String) : DisputeMilestoneRule {
    override fun check(item: DisputeMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeMilestoneAllowedCodeRule(var allowed: MutableList<String>) : DisputeMilestoneRule {
    override fun check(item: DisputeMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute milestone. */
data class DisputeMilestoneValidator(var rules: MutableList<DisputeMilestoneRule>) {
    fun validate(item: DisputeMilestone): MutableList<String> {
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

    fun requireValid(item: DisputeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeMilestoneValidator(): DisputeMilestoneValidator {
    return DisputeMilestoneValidator(
        mutableListOf<DisputeMilestoneRule>(
            DisputeMilestoneRequiredCodeRule("dispute milestone needs a value"),
            DisputeMilestoneLabelLengthRule(64),
            DisputeMilestoneUpdatedAtRangeRule(64, 1000),
            DisputeMilestoneLaneLengthRule(80),
            DisputeMilestoneRequiredLabelRule("dispute milestone needs this field"),
            DisputeMilestoneAllowedCodeRule(mutableListOf<String>("legacy", "regional", "fallback")),
        ),
    )
}
