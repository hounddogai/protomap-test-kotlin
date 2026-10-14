package com.acme.transport.safety.scorecard.tieredscorecardslot

/** A rule a batched scorecard plan must pass. Check returns its problems, or none. */
interface BatchedScorecardPlanRule {
    fun check(item: BatchedScorecardPlan): MutableList<String>
}

data class BatchedScorecardPlanCodePrefixRule(var prefix: String) : BatchedScorecardPlanRule {
    override fun check(item: BatchedScorecardPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardPlanOwnerIdLengthRule(var maxLength: Int) : BatchedScorecardPlanRule {
    override fun check(item: BatchedScorecardPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardPlanRequiredOwnerIdRule(var message: String) : BatchedScorecardPlanRule {
    override fun check(item: BatchedScorecardPlan): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardPlanRequiredCategoryRule(var message: String) : BatchedScorecardPlanRule {
    override fun check(item: BatchedScorecardPlan): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardPlanRequiredTitleRule(var message: String) : BatchedScorecardPlanRule {
    override fun check(item: BatchedScorecardPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched scorecard plan. */
data class BatchedScorecardPlanValidator(var rules: MutableList<BatchedScorecardPlanRule>) {
    fun validate(item: BatchedScorecardPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedScorecardPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedScorecardPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedScorecardPlanValidator(): BatchedScorecardPlanValidator {
    return BatchedScorecardPlanValidator(
        mutableListOf<BatchedScorecardPlanRule>(
            BatchedScorecardPlanCodePrefixRule("X"),
            BatchedScorecardPlanOwnerIdLengthRule(86400),
            BatchedScorecardPlanRequiredOwnerIdRule("batched scorecard plan needs this field"),
            BatchedScorecardPlanRequiredCategoryRule("batched scorecard plan needs this field"),
            BatchedScorecardPlanRequiredTitleRule("batched scorecard plan needs an entry"),
        ),
    )
}
