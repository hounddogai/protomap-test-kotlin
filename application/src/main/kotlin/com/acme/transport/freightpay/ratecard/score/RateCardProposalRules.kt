package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.kit.text.normalize

/** A rule a rate card proposal must pass. Check returns its problems, or none. */
interface RateCardProposalRule {
    fun check(item: RateCardProposal): MutableList<String>
}

data class RateCardProposalDepotLengthRule(var maxLength: Int) : RateCardProposalRule {
    override fun check(item: RateCardProposal): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardProposalCodePrefixRule(var prefix: String) : RateCardProposalRule {
    override fun check(item: RateCardProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardProposalRequiredDepotRule(var message: String) : RateCardProposalRule {
    override fun check(item: RateCardProposal): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardProposalAllowedDepotRule(var allowed: MutableList<String>) : RateCardProposalRule {
    override fun check(item: RateCardProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RateCardProposalRequiredCodeRule(var message: String) : RateCardProposalRule {
    override fun check(item: RateCardProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card proposal. */
data class RateCardProposalValidator(var rules: MutableList<RateCardProposalRule>) {
    fun validate(item: RateCardProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardProposalValidator(): RateCardProposalValidator {
    return RateCardProposalValidator(
        mutableListOf<RateCardProposalRule>(
            RateCardProposalDepotLengthRule(64),
            RateCardProposalCodePrefixRule("X"),
            RateCardProposalRequiredDepotRule("rate card proposal needs this field"),
            RateCardProposalAllowedDepotRule(mutableListOf<String>("capped")),
            RateCardProposalRequiredCodeRule("rate card proposal needs a value"),
        ),
    )
}
