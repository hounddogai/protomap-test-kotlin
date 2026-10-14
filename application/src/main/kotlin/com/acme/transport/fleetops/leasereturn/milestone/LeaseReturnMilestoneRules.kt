package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.kit.text.normalize

/** A rule a lease return milestone must pass. Check returns its problems, or none. */
interface LeaseReturnMilestoneRule {
    fun check(item: LeaseReturnMilestone): MutableList<String>
}

data class LeaseReturnMilestoneCodeLengthRule(var maxLength: Int) : LeaseReturnMilestoneRule {
    override fun check(item: LeaseReturnMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMilestoneAmountRangeRule(var min: Double, var max: Double) : LeaseReturnMilestoneRule {
    override fun check(item: LeaseReturnMilestone): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMilestoneNameLengthRule(var maxLength: Int) : LeaseReturnMilestoneRule {
    override fun check(item: LeaseReturnMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMilestoneScoreRangeRule(var min: Double, var max: Double) : LeaseReturnMilestoneRule {
    override fun check(item: LeaseReturnMilestone): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMilestoneVisibleGuardRule(var limit: Double) : LeaseReturnMilestoneRule {
    override fun check(item: LeaseReturnMilestone): MutableList<String> {
        if (item.visible && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMilestoneAllowedCodeRule(var allowed: MutableList<String>) : LeaseReturnMilestoneRule {
    override fun check(item: LeaseReturnMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return milestone. */
data class LeaseReturnMilestoneValidator(var rules: MutableList<LeaseReturnMilestoneRule>) {
    fun validate(item: LeaseReturnMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnMilestoneValidator(): LeaseReturnMilestoneValidator {
    return LeaseReturnMilestoneValidator(
        mutableListOf<LeaseReturnMilestoneRule>(
            LeaseReturnMilestoneCodeLengthRule(80),
            LeaseReturnMilestoneAmountRangeRule(2.0, 306.9),
            LeaseReturnMilestoneNameLengthRule(32),
            LeaseReturnMilestoneScoreRangeRule(5.1, 620.6),
            LeaseReturnMilestoneVisibleGuardRule(4.2),
            LeaseReturnMilestoneAllowedCodeRule(mutableListOf<String>("regional", "lenient", "weighted")),
        ),
    )
}
