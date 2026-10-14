package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.text.normalize

/** A rule a tiered roster milestone must pass. Check returns its problems, or none. */
interface TieredRosterMilestoneRule {
    fun check(item: TieredRosterMilestone): MutableList<String>
}

data class TieredRosterMilestoneQuantityRangeRule(var min: Int, var max: Int) : TieredRosterMilestoneRule {
    override fun check(item: TieredRosterMilestone): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class TieredRosterMilestoneAllowedNotesRule(var allowed: MutableList<String>) : TieredRosterMilestoneRule {
    override fun check(item: TieredRosterMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterMilestoneCodePrefixRule(var prefix: String) : TieredRosterMilestoneRule {
    override fun check(item: TieredRosterMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterMilestoneCodeLengthRule(var maxLength: Int) : TieredRosterMilestoneRule {
    override fun check(item: TieredRosterMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterMilestoneRequiredNotesRule(var message: String) : TieredRosterMilestoneRule {
    override fun check(item: TieredRosterMilestone): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterMilestoneRequiredTitleRule(var message: String) : TieredRosterMilestoneRule {
    override fun check(item: TieredRosterMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered roster milestone. */
data class TieredRosterMilestoneValidator(var rules: MutableList<TieredRosterMilestoneRule>) {
    fun validate(item: TieredRosterMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredRosterMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRosterMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRosterMilestoneValidator(): TieredRosterMilestoneValidator {
    return TieredRosterMilestoneValidator(
        mutableListOf<TieredRosterMilestoneRule>(
            TieredRosterMilestoneQuantityRangeRule(255, 3600),
            TieredRosterMilestoneAllowedNotesRule(mutableListOf<String>("rolling", "seasonal", "seasonal")),
            TieredRosterMilestoneCodePrefixRule("REF"),
            TieredRosterMilestoneCodeLengthRule(120),
            TieredRosterMilestoneRequiredNotesRule("tiered roster milestone needs an entry"),
            TieredRosterMilestoneRequiredTitleRule("tiered roster milestone needs an entry"),
        ),
    )
}
