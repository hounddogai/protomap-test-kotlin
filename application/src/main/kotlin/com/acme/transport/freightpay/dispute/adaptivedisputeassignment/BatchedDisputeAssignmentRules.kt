package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

/** A rule a batched dispute assignment must pass. Check returns its problems, or none. */
interface BatchedDisputeAssignmentRule {
    fun check(item: BatchedDisputeAssignment): MutableList<String>
}

data class BatchedDisputeAssignmentRevisionRangeRule(
    var min: Int,
    var max: Int,
) : BatchedDisputeAssignmentRule {
    override fun check(item: BatchedDisputeAssignment): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeAssignmentCodePrefixRule(var prefix: String) : BatchedDisputeAssignmentRule {
    override fun check(item: BatchedDisputeAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeAssignmentRequiredCodeRule(var message: String) : BatchedDisputeAssignmentRule {
    override fun check(item: BatchedDisputeAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dispute assignment. */
data class BatchedDisputeAssignmentValidator(var rules: MutableList<BatchedDisputeAssignmentRule>) {
    fun validate(item: BatchedDisputeAssignment): MutableList<String> {
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

    fun requireValid(item: BatchedDisputeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDisputeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDisputeAssignmentValidator(): BatchedDisputeAssignmentValidator {
    return BatchedDisputeAssignmentValidator(
        mutableListOf<BatchedDisputeAssignmentRule>(
            BatchedDisputeAssignmentRevisionRangeRule(120, 86400),
            BatchedDisputeAssignmentCodePrefixRule("X"),
            BatchedDisputeAssignmentRequiredCodeRule("batched dispute assignment needs this field"),
        ),
    )
}
