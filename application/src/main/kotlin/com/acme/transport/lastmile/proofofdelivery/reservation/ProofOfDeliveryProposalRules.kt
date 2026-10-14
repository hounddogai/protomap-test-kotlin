package com.acme.transport.lastmile.proofofdelivery.reservation

/** A rule a proof of delivery proposal must pass. Check returns its problems, or none. */
interface ProofOfDeliveryProposalRule {
    fun check(item: ProofOfDeliveryProposal): MutableList<String>
}

data class ProofOfDeliveryProposalCodePrefixRule(var prefix: String) : ProofOfDeliveryProposalRule {
    override fun check(item: ProofOfDeliveryProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryProposalRequiredTitleRule(var message: String) : ProofOfDeliveryProposalRule {
    override fun check(item: ProofOfDeliveryProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryProposalTitleLengthRule(var maxLength: Int) : ProofOfDeliveryProposalRule {
    override fun check(item: ProofOfDeliveryProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery proposal. */
data class ProofOfDeliveryProposalValidator(var rules: MutableList<ProofOfDeliveryProposalRule>) {
    fun validate(item: ProofOfDeliveryProposal): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryProposalValidator(): ProofOfDeliveryProposalValidator {
    return ProofOfDeliveryProposalValidator(
        mutableListOf<ProofOfDeliveryProposalRule>(
            ProofOfDeliveryProposalCodePrefixRule("OPS"),
            ProofOfDeliveryProposalRequiredTitleRule("proof of delivery proposal needs this field"),
            ProofOfDeliveryProposalTitleLengthRule(16),
        ),
    )
}
