package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.text.normalize

/** A rule a lenient accessorial proposal must pass. Check returns its problems, or none. */
interface LenientAccessorialProposalRule {
    fun check(item: LenientAccessorialProposal): MutableList<String>
}

data class LenientAccessorialProposalPhoneLengthRule(var maxLength: Int) : LenientAccessorialProposalRule {
    override fun check(item: LenientAccessorialProposal): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialProposalAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientAccessorialProposalRule {
    override fun check(item: LenientAccessorialProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialProposalRequiredCodeRule(var message: String) : LenientAccessorialProposalRule {
    override fun check(item: LenientAccessorialProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient accessorial proposal. */
data class LenientAccessorialProposalValidator(var rules: MutableList<LenientAccessorialProposalRule>) {
    fun validate(item: LenientAccessorialProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientAccessorialProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAccessorialProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAccessorialProposalValidator(): LenientAccessorialProposalValidator {
    return LenientAccessorialProposalValidator(
        mutableListOf<LenientAccessorialProposalRule>(
            LenientAccessorialProposalPhoneLengthRule(80),
            LenientAccessorialProposalAllowedCodeRule(mutableListOf<String>("seasonal", "tiered", "pending")),
            LenientAccessorialProposalRequiredCodeRule("lenient accessorial proposal needs this field"),
        ),
    )
}
