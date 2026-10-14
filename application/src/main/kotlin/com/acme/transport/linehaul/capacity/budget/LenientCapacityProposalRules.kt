package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.text.normalize

/** A rule a lenient capacity proposal must pass. Check returns its problems, or none. */
interface LenientCapacityProposalRule {
    fun check(item: LenientCapacityProposal): MutableList<String>
}

data class LenientCapacityProposalRequiredCodeRule(var message: String) : LenientCapacityProposalRule {
    override fun check(item: LenientCapacityProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityProposalAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientCapacityProposalRule {
    override fun check(item: LenientCapacityProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityProposalTitleLengthRule(var maxLength: Int) : LenientCapacityProposalRule {
    override fun check(item: LenientCapacityProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityProposalDepotLengthRule(var maxLength: Int) : LenientCapacityProposalRule {
    override fun check(item: LenientCapacityProposal): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient capacity proposal. */
data class LenientCapacityProposalValidator(var rules: MutableList<LenientCapacityProposalRule>) {
    fun validate(item: LenientCapacityProposal): MutableList<String> {
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

    fun requireValid(item: LenientCapacityProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCapacityProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCapacityProposalValidator(): LenientCapacityProposalValidator {
    return LenientCapacityProposalValidator(
        mutableListOf<LenientCapacityProposalRule>(
            LenientCapacityProposalRequiredCodeRule("lenient capacity proposal needs an entry"),
            LenientCapacityProposalAllowedCodeRule(mutableListOf<String>()),
            LenientCapacityProposalTitleLengthRule(16),
            LenientCapacityProposalDepotLengthRule(255),
        ),
    )
}
