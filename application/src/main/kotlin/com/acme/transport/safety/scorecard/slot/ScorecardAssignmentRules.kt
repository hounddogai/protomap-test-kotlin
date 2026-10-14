package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.text.normalize

/** A rule a scorecard assignment must pass. Check returns its problems, or none. */
interface ScorecardAssignmentRule {
    fun check(item: ScorecardAssignment): MutableList<String>
}

data class ScorecardAssignmentRequiredChannelRule(var message: String) : ScorecardAssignmentRule {
    override fun check(item: ScorecardAssignment): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAssignmentRequiredCodeRule(var message: String) : ScorecardAssignmentRule {
    override fun check(item: ScorecardAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAssignmentAllowedLabelRule(var allowed: MutableList<String>) : ScorecardAssignmentRule {
    override fun check(item: ScorecardAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAssignmentWindowDaysRangeRule(var min: Int, var max: Int) : ScorecardAssignmentRule {
    override fun check(item: ScorecardAssignment): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAssignmentDockLengthRule(var maxLength: Int) : ScorecardAssignmentRule {
    override fun check(item: ScorecardAssignment): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAssignmentRequiredLabelRule(var message: String) : ScorecardAssignmentRule {
    override fun check(item: ScorecardAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard assignment. */
data class ScorecardAssignmentValidator(var rules: MutableList<ScorecardAssignmentRule>) {
    fun validate(item: ScorecardAssignment): MutableList<String> {
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

    fun requireValid(item: ScorecardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardAssignmentValidator(): ScorecardAssignmentValidator {
    return ScorecardAssignmentValidator(
        mutableListOf<ScorecardAssignmentRule>(
            ScorecardAssignmentRequiredChannelRule("scorecard assignment needs an entry"),
            ScorecardAssignmentRequiredCodeRule("scorecard assignment needs a value"),
            ScorecardAssignmentAllowedLabelRule(mutableListOf<String>("manual", "strict", "regional")),
            ScorecardAssignmentWindowDaysRangeRule(120, 3600),
            ScorecardAssignmentDockLengthRule(3600),
            ScorecardAssignmentRequiredLabelRule("scorecard assignment needs an entry"),
        ),
    )
}
