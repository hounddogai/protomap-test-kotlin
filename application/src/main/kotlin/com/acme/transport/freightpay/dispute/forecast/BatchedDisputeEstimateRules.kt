package com.acme.transport.freightpay.dispute.forecast

/** A rule a batched dispute estimate must pass. Check returns its problems, or none. */
interface BatchedDisputeEstimateRule {
    fun check(item: BatchedDisputeEstimate): MutableList<String>
}

data class BatchedDisputeEstimateRequiredExternalRefRule(var message: String) : BatchedDisputeEstimateRule {
    override fun check(item: BatchedDisputeEstimate): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeEstimateRequiredNameRule(var message: String) : BatchedDisputeEstimateRule {
    override fun check(item: BatchedDisputeEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeEstimatePinnedGuardRule(var limit: Double) : BatchedDisputeEstimateRule {
    override fun check(item: BatchedDisputeEstimate): MutableList<String> {
        if (item.pinned && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeEstimateRequiredCodeRule(var message: String) : BatchedDisputeEstimateRule {
    override fun check(item: BatchedDisputeEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeEstimateCeilingRangeRule(
    var min: Double,
    var max: Double,
) : BatchedDisputeEstimateRule {
    override fun check(item: BatchedDisputeEstimate): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeEstimateRequiredCarrierRule(var message: String) : BatchedDisputeEstimateRule {
    override fun check(item: BatchedDisputeEstimate): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dispute estimate. */
data class BatchedDisputeEstimateValidator(var rules: MutableList<BatchedDisputeEstimateRule>) {
    fun validate(item: BatchedDisputeEstimate): MutableList<String> {
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

    fun requireValid(item: BatchedDisputeEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDisputeEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDisputeEstimateValidator(): BatchedDisputeEstimateValidator {
    return BatchedDisputeEstimateValidator(
        mutableListOf<BatchedDisputeEstimateRule>(
            BatchedDisputeEstimateRequiredExternalRefRule("batched dispute estimate needs a value"),
            BatchedDisputeEstimateRequiredNameRule("batched dispute estimate needs an entry"),
            BatchedDisputeEstimatePinnedGuardRule(9.9),
            BatchedDisputeEstimateRequiredCodeRule("batched dispute estimate needs an entry"),
            BatchedDisputeEstimateCeilingRangeRule(4.4, 2827.6),
            BatchedDisputeEstimateRequiredCarrierRule("batched dispute estimate needs an entry"),
        ),
    )
}
