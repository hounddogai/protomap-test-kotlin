package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.text.normalize

/** A rule a strict violation proposal must pass. Check returns its problems, or none. */
interface StrictViolationProposalRule {
    fun check(item: StrictViolationProposal): MutableList<String>
}

data class StrictViolationProposalCreatedAtRangeRule(var min: Int, var max: Int) : StrictViolationProposalRule {
    override fun check(item: StrictViolationProposal): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationProposalRequiredNameRule(var message: String) : StrictViolationProposalRule {
    override fun check(item: StrictViolationProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationProposalAllowedNameRule(
    var allowed: MutableList<String>,
) : StrictViolationProposalRule {
    override fun check(item: StrictViolationProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationProposalCeilingRangeRule(
    var min: Double,
    var max: Double,
) : StrictViolationProposalRule {
    override fun check(item: StrictViolationProposal): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class StrictViolationProposalScoreRangeRule(
    var min: Double,
    var max: Double,
) : StrictViolationProposalRule {
    override fun check(item: StrictViolationProposal): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict violation proposal. */
data class StrictViolationProposalValidator(var rules: MutableList<StrictViolationProposalRule>) {
    fun validate(item: StrictViolationProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictViolationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictViolationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictViolationProposalValidator(): StrictViolationProposalValidator {
    return StrictViolationProposalValidator(
        mutableListOf<StrictViolationProposalRule>(
            StrictViolationProposalCreatedAtRangeRule(80, 255),
            StrictViolationProposalRequiredNameRule("strict violation proposal needs this field"),
            StrictViolationProposalAllowedNameRule(mutableListOf<String>()),
            StrictViolationProposalCeilingRangeRule(9.2, 131.1),
            StrictViolationProposalScoreRangeRule(7.5, 3918.6),
        ),
    )
}
