package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.text.normalize

/** A rule a tire proposal must pass. Check returns its problems, or none. */
interface TireProposalRule {
    fun check(item: TireProposal): MutableList<String>
}

data class TireProposalTitleLengthRule(var maxLength: Int) : TireProposalRule {
    override fun check(item: TireProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireProposalRequiredDriverIdRule(var message: String) : TireProposalRule {
    override fun check(item: TireProposal): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireProposalLabelLengthRule(var maxLength: Int) : TireProposalRule {
    override fun check(item: TireProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireProposalAllowedLabelRule(var allowed: MutableList<String>) : TireProposalRule {
    override fun check(item: TireProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire proposal. */
data class TireProposalValidator(var rules: MutableList<TireProposalRule>) {
    fun validate(item: TireProposal): MutableList<String> {
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

    fun requireValid(item: TireProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultTireProposalValidator(): TireProposalValidator {
    return TireProposalValidator(
        mutableListOf<TireProposalRule>(
            TireProposalTitleLengthRule(1000),
            TireProposalRequiredDriverIdRule("tire proposal needs an entry"),
            TireProposalLabelLengthRule(255),
            TireProposalAllowedLabelRule(mutableListOf<String>("strict", "capped", "weighted")),
        ),
    )
}
