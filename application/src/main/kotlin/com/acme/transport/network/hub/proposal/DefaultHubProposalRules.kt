package com.acme.transport.network.hub.proposal

/** A rule a default hub proposal must pass. Check returns its problems, or none. */
interface DefaultHubProposalRule {
    fun check(item: DefaultHubProposal): MutableList<String>
}

data class DefaultHubProposalRequiredCodeRule(var message: String) : DefaultHubProposalRule {
    override fun check(item: DefaultHubProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubProposalTitleLengthRule(var maxLength: Int) : DefaultHubProposalRule {
    override fun check(item: DefaultHubProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubProposalPinnedGuardRule(var limit: Int) : DefaultHubProposalRule {
    override fun check(item: DefaultHubProposal): MutableList<String> {
        if (item.pinned && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubProposalRequiredTitleRule(var message: String) : DefaultHubProposalRule {
    override fun check(item: DefaultHubProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default hub proposal. */
data class DefaultHubProposalValidator(var rules: MutableList<DefaultHubProposalRule>) {
    fun validate(item: DefaultHubProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultHubProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultHubProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultHubProposalValidator(): DefaultHubProposalValidator {
    return DefaultHubProposalValidator(
        mutableListOf<DefaultHubProposalRule>(
            DefaultHubProposalRequiredCodeRule("default hub proposal needs a value"),
            DefaultHubProposalTitleLengthRule(80),
            DefaultHubProposalPinnedGuardRule(255),
            DefaultHubProposalRequiredTitleRule("default hub proposal needs a value"),
        ),
    )
}
