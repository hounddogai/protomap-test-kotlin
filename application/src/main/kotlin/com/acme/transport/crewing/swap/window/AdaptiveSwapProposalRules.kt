package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.text.normalize

/** A rule a adaptive swap proposal must pass. Check returns its problems, or none. */
interface AdaptiveSwapProposalRule {
    fun check(item: AdaptiveSwapProposal): MutableList<String>
}

data class AdaptiveSwapProposalAllowedLaneRule(var allowed: MutableList<String>) : AdaptiveSwapProposalRule {
    override fun check(item: AdaptiveSwapProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is expired")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapProposalRequiredCodeRule(var message: String) : AdaptiveSwapProposalRule {
    override fun check(item: AdaptiveSwapProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapProposalMinimumRangeRule(var min: Double, var max: Double) : AdaptiveSwapProposalRule {
    override fun check(item: AdaptiveSwapProposal): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class AdaptiveSwapProposalRequiredSummaryRule(var message: String) : AdaptiveSwapProposalRule {
    override fun check(item: AdaptiveSwapProposal): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive swap proposal. */
data class AdaptiveSwapProposalValidator(var rules: MutableList<AdaptiveSwapProposalRule>) {
    fun validate(item: AdaptiveSwapProposal): MutableList<String> {
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

    fun requireValid(item: AdaptiveSwapProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSwapProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSwapProposalValidator(): AdaptiveSwapProposalValidator {
    return AdaptiveSwapProposalValidator(
        mutableListOf<AdaptiveSwapProposalRule>(
            AdaptiveSwapProposalAllowedLaneRule(mutableListOf<String>()),
            AdaptiveSwapProposalRequiredCodeRule("adaptive swap proposal needs an entry"),
            AdaptiveSwapProposalMinimumRangeRule(5.1, 3500.5),
            AdaptiveSwapProposalRequiredSummaryRule("adaptive swap proposal needs a value"),
        ),
    )
}
