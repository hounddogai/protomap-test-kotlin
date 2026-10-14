package com.acme.transport.safety.nearmiss.proposal

/** A rule a capped near miss proposal must pass. Check returns its problems, or none. */
interface CappedNearMissProposalRule {
    fun check(item: CappedNearMissProposal): MutableList<String>
}

data class CappedNearMissProposalRequiredCodeRule(var message: String) : CappedNearMissProposalRule {
    override fun check(item: CappedNearMissProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissProposalCodePrefixRule(var prefix: String) : CappedNearMissProposalRule {
    override fun check(item: CappedNearMissProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissProposalRequiredChannelRule(var message: String) : CappedNearMissProposalRule {
    override fun check(item: CappedNearMissProposal): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissProposalRequiredDepotRule(var message: String) : CappedNearMissProposalRule {
    override fun check(item: CappedNearMissProposal): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissProposalDepotLengthRule(var maxLength: Int) : CappedNearMissProposalRule {
    override fun check(item: CappedNearMissProposal): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped near miss proposal. */
data class CappedNearMissProposalValidator(var rules: MutableList<CappedNearMissProposalRule>) {
    fun validate(item: CappedNearMissProposal): MutableList<String> {
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

    fun requireValid(item: CappedNearMissProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedNearMissProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedNearMissProposalValidator(): CappedNearMissProposalValidator {
    return CappedNearMissProposalValidator(
        mutableListOf<CappedNearMissProposalRule>(
            CappedNearMissProposalRequiredCodeRule("capped near miss proposal needs this field"),
            CappedNearMissProposalCodePrefixRule("REF"),
            CappedNearMissProposalRequiredChannelRule("capped near miss proposal needs this field"),
            CappedNearMissProposalRequiredDepotRule("capped near miss proposal needs an entry"),
            CappedNearMissProposalDepotLengthRule(255),
        ),
    )
}
