package com.acme.transport.customs.declaration.milestone

import com.acme.transport.kit.text.normalize

/** A rule a adaptive declaration milestone must pass. Check returns its problems, or none. */
interface AdaptiveDeclarationMilestoneRule {
    fun check(item: AdaptiveDeclarationMilestone): MutableList<String>
}

data class AdaptiveDeclarationMilestoneLaneLengthRule(var maxLength: Int) : AdaptiveDeclarationMilestoneRule {
    override fun check(item: AdaptiveDeclarationMilestone): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationMilestoneAllowedLaneRule(
    var allowed: MutableList<String>,
) : AdaptiveDeclarationMilestoneRule {
    override fun check(item: AdaptiveDeclarationMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationMilestonePriorityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveDeclarationMilestoneRule {
    override fun check(item: AdaptiveDeclarationMilestone): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationMilestoneCodeLengthRule(var maxLength: Int) : AdaptiveDeclarationMilestoneRule {
    override fun check(item: AdaptiveDeclarationMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive declaration milestone. */
data class AdaptiveDeclarationMilestoneValidator(var rules: MutableList<AdaptiveDeclarationMilestoneRule>) {
    fun validate(item: AdaptiveDeclarationMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDeclarationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDeclarationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDeclarationMilestoneValidator(): AdaptiveDeclarationMilestoneValidator {
    return AdaptiveDeclarationMilestoneValidator(
        mutableListOf<AdaptiveDeclarationMilestoneRule>(
            AdaptiveDeclarationMilestoneLaneLengthRule(255),
            AdaptiveDeclarationMilestoneAllowedLaneRule(mutableListOf<String>("capped", "regional")),
            AdaptiveDeclarationMilestonePriorityRangeRule(64, 1000),
            AdaptiveDeclarationMilestoneCodeLengthRule(32),
        ),
    )
}
