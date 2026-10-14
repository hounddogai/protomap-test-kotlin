package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice proposal must pass. Check returns its problems, or none. */
interface FreightInvoiceProposalRule {
    fun check(item: FreightInvoiceProposal): MutableList<String>
}

data class FreightInvoiceProposalCodePrefixRule(var prefix: String) : FreightInvoiceProposalRule {
    override fun check(item: FreightInvoiceProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceProposalTrailerIdLengthRule(var maxLength: Int) : FreightInvoiceProposalRule {
    override fun check(item: FreightInvoiceProposal): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceProposalRequiredCodeRule(var message: String) : FreightInvoiceProposalRule {
    override fun check(item: FreightInvoiceProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceProposalRequiredLabelRule(var message: String) : FreightInvoiceProposalRule {
    override fun check(item: FreightInvoiceProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceProposalRequiredSegmentRule(var message: String) : FreightInvoiceProposalRule {
    override fun check(item: FreightInvoiceProposal): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceProposalAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : FreightInvoiceProposalRule {
    override fun check(item: FreightInvoiceProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice proposal. */
data class FreightInvoiceProposalValidator(var rules: MutableList<FreightInvoiceProposalRule>) {
    fun validate(item: FreightInvoiceProposal): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceProposalValidator(): FreightInvoiceProposalValidator {
    return FreightInvoiceProposalValidator(
        mutableListOf<FreightInvoiceProposalRule>(
            FreightInvoiceProposalCodePrefixRule("OPS"),
            FreightInvoiceProposalTrailerIdLengthRule(16),
            FreightInvoiceProposalRequiredCodeRule("freight invoice proposal needs an entry"),
            FreightInvoiceProposalRequiredLabelRule("freight invoice proposal needs an entry"),
            FreightInvoiceProposalRequiredSegmentRule("freight invoice proposal needs an entry"),
            FreightInvoiceProposalAllowedTrailerIdRule(mutableListOf<String>()),
        ),
    )
}
