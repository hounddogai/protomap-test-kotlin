package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.text.normalize

/** A rule a batched proof of delivery budget must pass. Check returns its problems, or none. */
interface BatchedProofOfDeliveryBudgetRule {
    fun check(item: BatchedProofOfDeliveryBudget): MutableList<String>
}

data class BatchedProofOfDeliveryBudgetLabelLengthRule(var maxLength: Int) : BatchedProofOfDeliveryBudgetRule {
    override fun check(item: BatchedProofOfDeliveryBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryBudgetRequiredSummaryRule(
    var message: String,
) : BatchedProofOfDeliveryBudgetRule {
    override fun check(item: BatchedProofOfDeliveryBudget): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryBudgetVersionRangeRule(
    var min: Int,
    var max: Int,
) : BatchedProofOfDeliveryBudgetRule {
    override fun check(item: BatchedProofOfDeliveryBudget): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class BatchedProofOfDeliveryBudgetAllowedLabelRule(
    var allowed: MutableList<String>,
) : BatchedProofOfDeliveryBudgetRule {
    override fun check(item: BatchedProofOfDeliveryBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched proof of delivery budget. */
data class BatchedProofOfDeliveryBudgetValidator(var rules: MutableList<BatchedProofOfDeliveryBudgetRule>) {
    fun validate(item: BatchedProofOfDeliveryBudget): MutableList<String> {
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

    fun requireValid(item: BatchedProofOfDeliveryBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedProofOfDeliveryBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedProofOfDeliveryBudgetValidator(): BatchedProofOfDeliveryBudgetValidator {
    return BatchedProofOfDeliveryBudgetValidator(
        mutableListOf<BatchedProofOfDeliveryBudgetRule>(
            BatchedProofOfDeliveryBudgetLabelLengthRule(255),
            BatchedProofOfDeliveryBudgetRequiredSummaryRule("batched proof of delivery budget needs this field"),
            BatchedProofOfDeliveryBudgetVersionRangeRule(32, 64),
            BatchedProofOfDeliveryBudgetAllowedLabelRule(mutableListOf<String>("adaptive", "scheduled")),
        ),
    )
}
