package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a rest break proposal must pass. Check returns its problems, or none. */
interface RestBreakProposalRule {
    fun check(item: RestBreakProposal): MutableList<String>
}

data class RestBreakProposalAttemptsRangeRule(var min: Int, var max: Int) : RestBreakProposalRule {
    override fun check(item: RestBreakProposal): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakProposalRequiredCodeRule(var message: String) : RestBreakProposalRule {
    override fun check(item: RestBreakProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakProposalCodeLengthRule(var maxLength: Int) : RestBreakProposalRule {
    override fun check(item: RestBreakProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakProposalAllowedLabelRule(var allowed: MutableList<String>) : RestBreakProposalRule {
    override fun check(item: RestBreakProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakProposalMarginRangeRule(var min: Double, var max: Double) : RestBreakProposalRule {
    override fun check(item: RestBreakProposal): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class RestBreakProposalRequiredLabelRule(var message: String) : RestBreakProposalRule {
    override fun check(item: RestBreakProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break proposal. */
data class RestBreakProposalValidator(var rules: MutableList<RestBreakProposalRule>) {
    fun validate(item: RestBreakProposal): MutableList<String> {
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

    fun requireValid(item: RestBreakProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakProposalValidator(): RestBreakProposalValidator {
    return RestBreakProposalValidator(
        mutableListOf<RestBreakProposalRule>(
            RestBreakProposalAttemptsRangeRule(120, 3600),
            RestBreakProposalRequiredCodeRule("rest break proposal needs this field"),
            RestBreakProposalCodeLengthRule(1000),
            RestBreakProposalAllowedLabelRule(mutableListOf<String>()),
            RestBreakProposalMarginRangeRule(2.4, 4773.6),
            RestBreakProposalRequiredLabelRule("rest break proposal needs an entry"),
        ),
    )
}
