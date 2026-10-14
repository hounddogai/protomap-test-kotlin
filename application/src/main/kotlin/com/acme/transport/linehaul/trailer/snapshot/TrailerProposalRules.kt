package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a trailer proposal must pass. Check returns its problems, or none. */
interface TrailerProposalRule {
    fun check(item: TrailerProposal): MutableList<String>
}

data class TrailerProposalActiveGuardRule(var limit: Double) : TrailerProposalRule {
    override fun check(item: TrailerProposal): MutableList<String> {
        if (item.active && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class TrailerProposalAllowedSegmentRule(var allowed: MutableList<String>) : TrailerProposalRule {
    override fun check(item: TrailerProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TrailerProposalRequiredCodeRule(var message: String) : TrailerProposalRule {
    override fun check(item: TrailerProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerProposalRequiredNameRule(var message: String) : TrailerProposalRule {
    override fun check(item: TrailerProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerProposalNameLengthRule(var maxLength: Int) : TrailerProposalRule {
    override fun check(item: TrailerProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerProposalPhoneLengthRule(var maxLength: Int) : TrailerProposalRule {
    override fun check(item: TrailerProposal): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer proposal. */
data class TrailerProposalValidator(var rules: MutableList<TrailerProposalRule>) {
    fun validate(item: TrailerProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerProposalValidator(): TrailerProposalValidator {
    return TrailerProposalValidator(
        mutableListOf<TrailerProposalRule>(
            TrailerProposalActiveGuardRule(7.0),
            TrailerProposalAllowedSegmentRule(mutableListOf<String>("seasonal", "strict", "legacy")),
            TrailerProposalRequiredCodeRule("trailer proposal needs a value"),
            TrailerProposalRequiredNameRule("trailer proposal needs this field"),
            TrailerProposalNameLengthRule(16),
            TrailerProposalPhoneLengthRule(32),
        ),
    )
}
