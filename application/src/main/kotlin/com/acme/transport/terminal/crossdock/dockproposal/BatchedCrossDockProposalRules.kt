package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.normalize

/** A rule a batched cross dock proposal must pass. Check returns its problems, or none. */
interface BatchedCrossDockProposalRule {
    fun check(item: BatchedCrossDockProposal): MutableList<String>
}

data class BatchedCrossDockProposalRetriesRangeRule(var min: Int, var max: Int) : BatchedCrossDockProposalRule {
    override fun check(item: BatchedCrossDockProposal): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockProposalRevisionRangeRule(
    var min: Int,
    var max: Int,
) : BatchedCrossDockProposalRule {
    override fun check(item: BatchedCrossDockProposal): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockProposalDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCrossDockProposalRule {
    override fun check(item: BatchedCrossDockProposal): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class BatchedCrossDockProposalTitleLengthRule(var maxLength: Int) : BatchedCrossDockProposalRule {
    override fun check(item: BatchedCrossDockProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockProposalAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : BatchedCrossDockProposalRule {
    override fun check(item: BatchedCrossDockProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockProposalCodePrefixRule(var prefix: String) : BatchedCrossDockProposalRule {
    override fun check(item: BatchedCrossDockProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched cross dock proposal. */
data class BatchedCrossDockProposalValidator(var rules: MutableList<BatchedCrossDockProposalRule>) {
    fun validate(item: BatchedCrossDockProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCrossDockProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCrossDockProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCrossDockProposalValidator(): BatchedCrossDockProposalValidator {
    return BatchedCrossDockProposalValidator(
        mutableListOf<BatchedCrossDockProposalRule>(
            BatchedCrossDockProposalRetriesRangeRule(16, 3600),
            BatchedCrossDockProposalRevisionRangeRule(16, 86400),
            BatchedCrossDockProposalDistanceKmRangeRule(6.6, 634.7),
            BatchedCrossDockProposalTitleLengthRule(16),
            BatchedCrossDockProposalAllowedOwnerIdRule(mutableListOf<String>("scheduled")),
            BatchedCrossDockProposalCodePrefixRule("Q"),
        ),
    )
}
