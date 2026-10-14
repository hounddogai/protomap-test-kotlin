package com.acme.transport.safety.scorecard.scorecardgroup

/** A rule a batched scorecard milestone must pass. Check returns its problems, or none. */
interface BatchedScorecardMilestoneRule {
    fun check(item: BatchedScorecardMilestone): MutableList<String>
}

data class BatchedScorecardMilestoneCodeLengthRule(var maxLength: Int) : BatchedScorecardMilestoneRule {
    override fun check(item: BatchedScorecardMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardMilestoneRequiredTitleRule(var message: String) : BatchedScorecardMilestoneRule {
    override fun check(item: BatchedScorecardMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardMilestoneTitleLengthRule(var maxLength: Int) : BatchedScorecardMilestoneRule {
    override fun check(item: BatchedScorecardMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardMilestoneRequiredSummaryRule(var message: String) : BatchedScorecardMilestoneRule {
    override fun check(item: BatchedScorecardMilestone): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardMilestoneUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : BatchedScorecardMilestoneRule {
    override fun check(item: BatchedScorecardMilestone): MutableList<String> {
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

/** Runs every rule of a batched scorecard milestone. */
data class BatchedScorecardMilestoneValidator(var rules: MutableList<BatchedScorecardMilestoneRule>) {
    fun validate(item: BatchedScorecardMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedScorecardMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedScorecardMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedScorecardMilestoneValidator(): BatchedScorecardMilestoneValidator {
    return BatchedScorecardMilestoneValidator(
        mutableListOf<BatchedScorecardMilestoneRule>(
            BatchedScorecardMilestoneCodeLengthRule(80),
            BatchedScorecardMilestoneRequiredTitleRule("batched scorecard milestone needs an entry"),
            BatchedScorecardMilestoneTitleLengthRule(64),
            BatchedScorecardMilestoneRequiredSummaryRule("batched scorecard milestone needs an entry"),
            BatchedScorecardMilestoneUpdatedAtRangeRule(1000, 1000),
        ),
    )
}
