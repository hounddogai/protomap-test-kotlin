package com.acme.transport.network.hub.hubestimate

/** A rule a strict hub proposal must pass. Check returns its problems, or none. */
interface StrictHubProposalRule {
    fun check(item: StrictHubProposal): MutableList<String>
}

data class StrictHubProposalRequiredTitleRule(var message: String) : StrictHubProposalRule {
    override fun check(item: StrictHubProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictHubProposalCodePrefixRule(var prefix: String) : StrictHubProposalRule {
    override fun check(item: StrictHubProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictHubProposalRequiredTrailerIdRule(var message: String) : StrictHubProposalRule {
    override fun check(item: StrictHubProposal): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict hub proposal. */
data class StrictHubProposalValidator(var rules: MutableList<StrictHubProposalRule>) {
    fun validate(item: StrictHubProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictHubProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictHubProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictHubProposalValidator(): StrictHubProposalValidator {
    return StrictHubProposalValidator(
        mutableListOf<StrictHubProposalRule>(
            StrictHubProposalRequiredTitleRule("strict hub proposal needs this field"),
            StrictHubProposalCodePrefixRule("OPS"),
            StrictHubProposalRequiredTrailerIdRule("strict hub proposal needs a value"),
        ),
    )
}
