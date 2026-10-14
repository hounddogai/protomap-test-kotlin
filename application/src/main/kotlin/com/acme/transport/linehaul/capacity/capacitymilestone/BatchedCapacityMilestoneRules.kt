package com.acme.transport.linehaul.capacity.capacitymilestone

/** A rule a batched capacity milestone must pass. Check returns its problems, or none. */
interface BatchedCapacityMilestoneRule {
    fun check(item: BatchedCapacityMilestone): MutableList<String>
}

data class BatchedCapacityMilestoneCarrierLengthRule(var maxLength: Int) : BatchedCapacityMilestoneRule {
    override fun check(item: BatchedCapacityMilestone): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityMilestoneEndsAtRangeRule(var min: Int, var max: Int) : BatchedCapacityMilestoneRule {
    override fun check(item: BatchedCapacityMilestone): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class BatchedCapacityMilestoneRequiredCodeRule(var message: String) : BatchedCapacityMilestoneRule {
    override fun check(item: BatchedCapacityMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched capacity milestone. */
data class BatchedCapacityMilestoneValidator(var rules: MutableList<BatchedCapacityMilestoneRule>) {
    fun validate(item: BatchedCapacityMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCapacityMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCapacityMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCapacityMilestoneValidator(): BatchedCapacityMilestoneValidator {
    return BatchedCapacityMilestoneValidator(
        mutableListOf<BatchedCapacityMilestoneRule>(
            BatchedCapacityMilestoneCarrierLengthRule(120),
            BatchedCapacityMilestoneEndsAtRangeRule(80, 120),
            BatchedCapacityMilestoneRequiredCodeRule("batched capacity milestone needs an entry"),
        ),
    )
}
