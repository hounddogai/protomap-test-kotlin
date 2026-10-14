package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.kit.text.normalize

/** A rule a attempt proposal must pass. Check returns its problems, or none. */
interface AttemptProposalRule {
    fun check(item: AttemptProposal): MutableList<String>
}

data class AttemptProposalAllowedLabelRule(var allowed: MutableList<String>) : AttemptProposalRule {
    override fun check(item: AttemptProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class AttemptProposalRequiredSegmentRule(var message: String) : AttemptProposalRule {
    override fun check(item: AttemptProposal): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptProposalAttemptsRangeRule(var min: Int, var max: Int) : AttemptProposalRule {
    override fun check(item: AttemptProposal): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class AttemptProposalLabelLengthRule(var maxLength: Int) : AttemptProposalRule {
    override fun check(item: AttemptProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt proposal. */
data class AttemptProposalValidator(var rules: MutableList<AttemptProposalRule>) {
    fun validate(item: AttemptProposal): MutableList<String> {
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

    fun requireValid(item: AttemptProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptProposalValidator(): AttemptProposalValidator {
    return AttemptProposalValidator(
        mutableListOf<AttemptProposalRule>(
            AttemptProposalAllowedLabelRule(mutableListOf<String>("regional")),
            AttemptProposalRequiredSegmentRule("attempt proposal needs an entry"),
            AttemptProposalAttemptsRangeRule(3600, 86400),
            AttemptProposalLabelLengthRule(86400),
        ),
    )
}
