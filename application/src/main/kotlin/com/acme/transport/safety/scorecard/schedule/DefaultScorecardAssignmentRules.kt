package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.text.normalize

/** A rule a default scorecard assignment must pass. Check returns its problems, or none. */
interface DefaultScorecardAssignmentRule {
    fun check(item: DefaultScorecardAssignment): MutableList<String>
}

data class DefaultScorecardAssignmentStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : DefaultScorecardAssignmentRule {
    override fun check(item: DefaultScorecardAssignment): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardAssignmentRequiredNameRule(var message: String) : DefaultScorecardAssignmentRule {
    override fun check(item: DefaultScorecardAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardAssignmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : DefaultScorecardAssignmentRule {
    override fun check(item: DefaultScorecardAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardAssignmentRequiredCodeRule(var message: String) : DefaultScorecardAssignmentRule {
    override fun check(item: DefaultScorecardAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default scorecard assignment. */
data class DefaultScorecardAssignmentValidator(var rules: MutableList<DefaultScorecardAssignmentRule>) {
    fun validate(item: DefaultScorecardAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultScorecardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultScorecardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultScorecardAssignmentValidator(): DefaultScorecardAssignmentValidator {
    return DefaultScorecardAssignmentValidator(
        mutableListOf<DefaultScorecardAssignmentRule>(
            DefaultScorecardAssignmentStartsAtRangeRule(16, 255),
            DefaultScorecardAssignmentRequiredNameRule("default scorecard assignment needs this field"),
            DefaultScorecardAssignmentAllowedCodeRule(mutableListOf<String>("primary")),
            DefaultScorecardAssignmentRequiredCodeRule("default scorecard assignment needs a value"),
        ),
    )
}
