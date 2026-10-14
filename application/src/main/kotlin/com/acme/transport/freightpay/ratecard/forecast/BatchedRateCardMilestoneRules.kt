package com.acme.transport.freightpay.ratecard.forecast

/** A rule a batched rate card milestone must pass. Check returns its problems, or none. */
interface BatchedRateCardMilestoneRule {
    fun check(item: BatchedRateCardMilestone): MutableList<String>
}

data class BatchedRateCardMilestoneRequiredRouteCodeRule(var message: String) : BatchedRateCardMilestoneRule {
    override fun check(item: BatchedRateCardMilestone): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRateCardMilestoneRetriesRangeRule(var min: Int, var max: Int) : BatchedRateCardMilestoneRule {
    override fun check(item: BatchedRateCardMilestone): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class BatchedRateCardMilestoneRouteCodeLengthRule(var maxLength: Int) : BatchedRateCardMilestoneRule {
    override fun check(item: BatchedRateCardMilestone): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched rate card milestone. */
data class BatchedRateCardMilestoneValidator(var rules: MutableList<BatchedRateCardMilestoneRule>) {
    fun validate(item: BatchedRateCardMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRateCardMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRateCardMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRateCardMilestoneValidator(): BatchedRateCardMilestoneValidator {
    return BatchedRateCardMilestoneValidator(
        mutableListOf<BatchedRateCardMilestoneRule>(
            BatchedRateCardMilestoneRequiredRouteCodeRule("batched rate card milestone needs this field"),
            BatchedRateCardMilestoneRetriesRangeRule(64, 1000),
            BatchedRateCardMilestoneRouteCodeLengthRule(255),
        ),
    )
}
