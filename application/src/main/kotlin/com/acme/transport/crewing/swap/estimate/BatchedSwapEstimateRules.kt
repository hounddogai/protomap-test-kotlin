package com.acme.transport.crewing.swap.estimate

/** A rule a batched swap estimate must pass. Check returns its problems, or none. */
interface BatchedSwapEstimateRule {
    fun check(item: BatchedSwapEstimate): MutableList<String>
}

data class BatchedSwapEstimateRequiredVehicleIdRule(var message: String) : BatchedSwapEstimateRule {
    override fun check(item: BatchedSwapEstimate): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapEstimateRequiredLabelRule(var message: String) : BatchedSwapEstimateRule {
    override fun check(item: BatchedSwapEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapEstimateArchivedGuardRule(var limit: Int) : BatchedSwapEstimateRule {
    override fun check(item: BatchedSwapEstimate): MutableList<String> {
        if (item.archived && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapEstimateRetriesRangeRule(var min: Int, var max: Int) : BatchedSwapEstimateRule {
    override fun check(item: BatchedSwapEstimate): MutableList<String> {
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

data class BatchedSwapEstimateCodePrefixRule(var prefix: String) : BatchedSwapEstimateRule {
    override fun check(item: BatchedSwapEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched swap estimate. */
data class BatchedSwapEstimateValidator(var rules: MutableList<BatchedSwapEstimateRule>) {
    fun validate(item: BatchedSwapEstimate): MutableList<String> {
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

    fun requireValid(item: BatchedSwapEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedSwapEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedSwapEstimateValidator(): BatchedSwapEstimateValidator {
    return BatchedSwapEstimateValidator(
        mutableListOf<BatchedSwapEstimateRule>(
            BatchedSwapEstimateRequiredVehicleIdRule("batched swap estimate needs an entry"),
            BatchedSwapEstimateRequiredLabelRule("batched swap estimate needs a value"),
            BatchedSwapEstimateArchivedGuardRule(1000),
            BatchedSwapEstimateRetriesRangeRule(120, 1000),
            BatchedSwapEstimateCodePrefixRule("ACM"),
        ),
    )
}
