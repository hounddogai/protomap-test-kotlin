package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.text.normalize

/** A rule a scheduled violation proposal must pass. Check returns its problems, or none. */
interface ScheduledViolationProposalRule {
    fun check(item: ScheduledViolationProposal): MutableList<String>
}

data class ScheduledViolationProposalWeightRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledViolationProposalRule {
    override fun check(item: ScheduledViolationProposal): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledViolationProposalCodeLengthRule(var maxLength: Int) : ScheduledViolationProposalRule {
    override fun check(item: ScheduledViolationProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledViolationProposalCodePrefixRule(var prefix: String) : ScheduledViolationProposalRule {
    override fun check(item: ScheduledViolationProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledViolationProposalRequiredTitleRule(var message: String) : ScheduledViolationProposalRule {
    override fun check(item: ScheduledViolationProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledViolationProposalAllowedTitleRule(
    var allowed: MutableList<String>,
) : ScheduledViolationProposalRule {
    override fun check(item: ScheduledViolationProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled violation proposal. */
data class ScheduledViolationProposalValidator(var rules: MutableList<ScheduledViolationProposalRule>) {
    fun validate(item: ScheduledViolationProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledViolationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledViolationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledViolationProposalValidator(): ScheduledViolationProposalValidator {
    return ScheduledViolationProposalValidator(
        mutableListOf<ScheduledViolationProposalRule>(
            ScheduledViolationProposalWeightRangeRule(9.8, 4622.5),
            ScheduledViolationProposalCodeLengthRule(3600),
            ScheduledViolationProposalCodePrefixRule("Q"),
            ScheduledViolationProposalRequiredTitleRule("scheduled violation proposal needs this field"),
            ScheduledViolationProposalAllowedTitleRule(mutableListOf<String>("rolling", "regional")),
        ),
    )
}
