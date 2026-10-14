package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.text.normalize

/** A rule a batched tender proposal must pass. Check returns its problems, or none. */
interface BatchedTenderProposalRule {
    fun check(item: BatchedTenderProposal): MutableList<String>
}

data class BatchedTenderProposalAllowedCodeRule(var allowed: MutableList<String>) : BatchedTenderProposalRule {
    override fun check(item: BatchedTenderProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTenderProposalDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : BatchedTenderProposalRule {
    override fun check(item: BatchedTenderProposal): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTenderProposalNameLengthRule(var maxLength: Int) : BatchedTenderProposalRule {
    override fun check(item: BatchedTenderProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tender proposal. */
data class BatchedTenderProposalValidator(var rules: MutableList<BatchedTenderProposalRule>) {
    fun validate(item: BatchedTenderProposal): MutableList<String> {
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

    fun requireValid(item: BatchedTenderProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTenderProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTenderProposalValidator(): BatchedTenderProposalValidator {
    return BatchedTenderProposalValidator(
        mutableListOf<BatchedTenderProposalRule>(
            BatchedTenderProposalAllowedCodeRule(mutableListOf<String>()),
            BatchedTenderProposalDistanceKmRangeRule(0.7, 4295.9),
            BatchedTenderProposalNameLengthRule(86400),
        ),
    )
}
