package com.acme.transport.safety.scorecard.scorecardnotice

/** A rule a batched scorecard proposal must pass. Check returns its problems, or none. */
interface BatchedScorecardProposalRule {
    fun check(item: BatchedScorecardProposal): MutableList<String>
}

data class BatchedScorecardProposalLabelLengthRule(var maxLength: Int) : BatchedScorecardProposalRule {
    override fun check(item: BatchedScorecardProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardProposalUnitLengthRule(var maxLength: Int) : BatchedScorecardProposalRule {
    override fun check(item: BatchedScorecardProposal): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardProposalUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : BatchedScorecardProposalRule {
    override fun check(item: BatchedScorecardProposal): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class BatchedScorecardProposalCodePrefixRule(var prefix: String) : BatchedScorecardProposalRule {
    override fun check(item: BatchedScorecardProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched scorecard proposal. */
data class BatchedScorecardProposalValidator(var rules: MutableList<BatchedScorecardProposalRule>) {
    fun validate(item: BatchedScorecardProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedScorecardProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedScorecardProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedScorecardProposalValidator(): BatchedScorecardProposalValidator {
    return BatchedScorecardProposalValidator(
        mutableListOf<BatchedScorecardProposalRule>(
            BatchedScorecardProposalLabelLengthRule(3600),
            BatchedScorecardProposalUnitLengthRule(80),
            BatchedScorecardProposalUpdatedAtRangeRule(80, 120),
            BatchedScorecardProposalCodePrefixRule("OPS"),
        ),
    )
}
