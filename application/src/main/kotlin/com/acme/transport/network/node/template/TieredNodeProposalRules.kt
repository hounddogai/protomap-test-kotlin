package com.acme.transport.network.node.template

import com.acme.transport.kit.text.normalize

/** A rule a tiered node proposal must pass. Check returns its problems, or none. */
interface TieredNodeProposalRule {
    fun check(item: TieredNodeProposal): MutableList<String>
}

data class TieredNodeProposalAllowedLabelRule(var allowed: MutableList<String>) : TieredNodeProposalRule {
    override fun check(item: TieredNodeProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeProposalRequiredSummaryRule(var message: String) : TieredNodeProposalRule {
    override fun check(item: TieredNodeProposal): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeProposalCodePrefixRule(var prefix: String) : TieredNodeProposalRule {
    override fun check(item: TieredNodeProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeProposalRequiredCodeRule(var message: String) : TieredNodeProposalRule {
    override fun check(item: TieredNodeProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeProposalRequiredLabelRule(var message: String) : TieredNodeProposalRule {
    override fun check(item: TieredNodeProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeProposalLabelLengthRule(var maxLength: Int) : TieredNodeProposalRule {
    override fun check(item: TieredNodeProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered node proposal. */
data class TieredNodeProposalValidator(var rules: MutableList<TieredNodeProposalRule>) {
    fun validate(item: TieredNodeProposal): MutableList<String> {
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

    fun requireValid(item: TieredNodeProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredNodeProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredNodeProposalValidator(): TieredNodeProposalValidator {
    return TieredNodeProposalValidator(
        mutableListOf<TieredNodeProposalRule>(
            TieredNodeProposalAllowedLabelRule(mutableListOf<String>("primary", "lenient", "rolling")),
            TieredNodeProposalRequiredSummaryRule("tiered node proposal needs this field"),
            TieredNodeProposalCodePrefixRule("X"),
            TieredNodeProposalRequiredCodeRule("tiered node proposal needs this field"),
            TieredNodeProposalRequiredLabelRule("tiered node proposal needs this field"),
            TieredNodeProposalLabelLengthRule(120),
        ),
    )
}
