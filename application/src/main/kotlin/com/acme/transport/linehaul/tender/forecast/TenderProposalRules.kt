package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.kit.text.normalize

/** A rule a tender proposal must pass. Check returns its problems, or none. */
interface TenderProposalRule {
    fun check(item: TenderProposal): MutableList<String>
}

data class TenderProposalContactEmailLengthRule(var maxLength: Int) : TenderProposalRule {
    override fun check(item: TenderProposal): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderProposalAllowedDescriptionRule(var allowed: MutableList<String>) : TenderProposalRule {
    override fun check(item: TenderProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TenderProposalLabelLengthRule(var maxLength: Int) : TenderProposalRule {
    override fun check(item: TenderProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderProposalRequiredDescriptionRule(var message: String) : TenderProposalRule {
    override fun check(item: TenderProposal): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderProposalRequiredContactEmailRule(var message: String) : TenderProposalRule {
    override fun check(item: TenderProposal): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender proposal. */
data class TenderProposalValidator(var rules: MutableList<TenderProposalRule>) {
    fun validate(item: TenderProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderProposalValidator(): TenderProposalValidator {
    return TenderProposalValidator(
        mutableListOf<TenderProposalRule>(
            TenderProposalContactEmailLengthRule(32),
            TenderProposalAllowedDescriptionRule(mutableListOf<String>()),
            TenderProposalLabelLengthRule(64),
            TenderProposalRequiredDescriptionRule("tender proposal needs an entry"),
            TenderProposalRequiredContactEmailRule("tender proposal needs an entry"),
        ),
    )
}
