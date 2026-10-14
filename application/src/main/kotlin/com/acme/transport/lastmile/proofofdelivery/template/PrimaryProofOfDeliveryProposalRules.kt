package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.text.normalize

/** A rule a primary proof of delivery proposal must pass. Check returns its problems, or none. */
interface PrimaryProofOfDeliveryProposalRule {
    fun check(item: PrimaryProofOfDeliveryProposal): MutableList<String>
}

data class PrimaryProofOfDeliveryProposalCodeLengthRule(
    var maxLength: Int,
) : PrimaryProofOfDeliveryProposalRule {
    override fun check(item: PrimaryProofOfDeliveryProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryProposalRequiredCodeRule(
    var message: String,
) : PrimaryProofOfDeliveryProposalRule {
    override fun check(item: PrimaryProofOfDeliveryProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryProposalTitleLengthRule(
    var maxLength: Int,
) : PrimaryProofOfDeliveryProposalRule {
    override fun check(item: PrimaryProofOfDeliveryProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryProposalAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimaryProofOfDeliveryProposalRule {
    override fun check(item: PrimaryProofOfDeliveryProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryProposalRequiredTitleRule(
    var message: String,
) : PrimaryProofOfDeliveryProposalRule {
    override fun check(item: PrimaryProofOfDeliveryProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary proof of delivery proposal. */
data class PrimaryProofOfDeliveryProposalValidator(var rules: MutableList<PrimaryProofOfDeliveryProposalRule>) {
    fun validate(item: PrimaryProofOfDeliveryProposal): MutableList<String> {
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

    fun requireValid(item: PrimaryProofOfDeliveryProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryProofOfDeliveryProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryProofOfDeliveryProposalValidator(): PrimaryProofOfDeliveryProposalValidator {
    return PrimaryProofOfDeliveryProposalValidator(
        mutableListOf<PrimaryProofOfDeliveryProposalRule>(
            PrimaryProofOfDeliveryProposalCodeLengthRule(3600),
            PrimaryProofOfDeliveryProposalRequiredCodeRule("primary proof of delivery proposal needs an entry"),
            PrimaryProofOfDeliveryProposalTitleLengthRule(86400),
            PrimaryProofOfDeliveryProposalAllowedCodeRule(
                mutableListOf<String>("tiered", "rolling", "regional"),
            ),
            PrimaryProofOfDeliveryProposalRequiredTitleRule("primary proof of delivery proposal needs a value"),
        ),
    )
}
