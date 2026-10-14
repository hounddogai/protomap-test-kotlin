package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.text.normalize

/** A rule a scorecard milestone must pass. Check returns its problems, or none. */
interface ScorecardMilestoneRule {
    fun check(item: ScorecardMilestone): MutableList<String>
}

data class ScorecardMilestoneLabelLengthRule(var maxLength: Int) : ScorecardMilestoneRule {
    override fun check(item: ScorecardMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardMilestoneAllowedLabelRule(var allowed: MutableList<String>) : ScorecardMilestoneRule {
    override fun check(item: ScorecardMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardMilestoneCodePrefixRule(var prefix: String) : ScorecardMilestoneRule {
    override fun check(item: ScorecardMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard milestone. */
data class ScorecardMilestoneValidator(var rules: MutableList<ScorecardMilestoneRule>) {
    fun validate(item: ScorecardMilestone): MutableList<String> {
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

    fun requireValid(item: ScorecardMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardMilestoneValidator(): ScorecardMilestoneValidator {
    return ScorecardMilestoneValidator(
        mutableListOf<ScorecardMilestoneRule>(
            ScorecardMilestoneLabelLengthRule(255),
            ScorecardMilestoneAllowedLabelRule(mutableListOf<String>("legacy")),
            ScorecardMilestoneCodePrefixRule("X"),
        ),
    )
}
