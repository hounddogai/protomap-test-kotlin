package com.acme.transport.customs.declaration.baseline

import com.acme.transport.kit.text.normalize

/** A rule a declaration milestone must pass. Check returns its problems, or none. */
interface DeclarationMilestoneRule {
    fun check(item: DeclarationMilestone): MutableList<String>
}

data class DeclarationMilestoneLimitRangeRule(var min: Int, var max: Int) : DeclarationMilestoneRule {
    override fun check(item: DeclarationMilestone): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class DeclarationMilestoneRequiredTrailerIdRule(var message: String) : DeclarationMilestoneRule {
    override fun check(item: DeclarationMilestone): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationMilestoneAllowedTitleRule(var allowed: MutableList<String>) : DeclarationMilestoneRule {
    override fun check(item: DeclarationMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationMilestoneLabelLengthRule(var maxLength: Int) : DeclarationMilestoneRule {
    override fun check(item: DeclarationMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationMilestoneApprovedGuardRule(var limit: Int) : DeclarationMilestoneRule {
    override fun check(item: DeclarationMilestone): MutableList<String> {
        if (item.approved && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationMilestoneStartsAtRangeRule(var min: Int, var max: Int) : DeclarationMilestoneRule {
    override fun check(item: DeclarationMilestone): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration milestone. */
data class DeclarationMilestoneValidator(var rules: MutableList<DeclarationMilestoneRule>) {
    fun validate(item: DeclarationMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationMilestoneValidator(): DeclarationMilestoneValidator {
    return DeclarationMilestoneValidator(
        mutableListOf<DeclarationMilestoneRule>(
            DeclarationMilestoneLimitRangeRule(32, 3600),
            DeclarationMilestoneRequiredTrailerIdRule("declaration milestone needs a value"),
            DeclarationMilestoneAllowedTitleRule(mutableListOf<String>()),
            DeclarationMilestoneLabelLengthRule(3600),
            DeclarationMilestoneApprovedGuardRule(86400),
            DeclarationMilestoneStartsAtRangeRule(32, 86400),
        ),
    )
}
