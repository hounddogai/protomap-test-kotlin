package com.acme.transport.lastmile.attempt.batchedattemptrequest

/** A rule a batched attempt proposal must pass. Check returns its problems, or none. */
interface BatchedAttemptProposalRule {
    fun check(item: BatchedAttemptProposal): MutableList<String>
}

data class BatchedAttemptProposalRequiredLabelRule(var message: String) : BatchedAttemptProposalRule {
    override fun check(item: BatchedAttemptProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAttemptProposalLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : BatchedAttemptProposalRule {
    override fun check(item: BatchedAttemptProposal): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class BatchedAttemptProposalBatchSizeRangeRule(var min: Int, var max: Int) : BatchedAttemptProposalRule {
    override fun check(item: BatchedAttemptProposal): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class BatchedAttemptProposalMinimumRangeRule(
    var min: Double,
    var max: Double,
) : BatchedAttemptProposalRule {
    override fun check(item: BatchedAttemptProposal): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAttemptProposalRequiredContactEmailRule(var message: String) : BatchedAttemptProposalRule {
    override fun check(item: BatchedAttemptProposal): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched attempt proposal. */
data class BatchedAttemptProposalValidator(var rules: MutableList<BatchedAttemptProposalRule>) {
    fun validate(item: BatchedAttemptProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAttemptProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAttemptProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAttemptProposalValidator(): BatchedAttemptProposalValidator {
    return BatchedAttemptProposalValidator(
        mutableListOf<BatchedAttemptProposalRule>(
            BatchedAttemptProposalRequiredLabelRule("batched attempt proposal needs this field"),
            BatchedAttemptProposalLoadKgRangeRule(9.8, 2976.1),
            BatchedAttemptProposalBatchSizeRangeRule(80, 1000),
            BatchedAttemptProposalMinimumRangeRule(6.6, 2717.9),
            BatchedAttemptProposalRequiredContactEmailRule("batched attempt proposal needs a value"),
        ),
    )
}
