package com.acme.transport.network.hub.primaryhubsummary

/** A rule a batched hub tier must pass. Check returns its problems, or none. */
interface BatchedHubTierRule {
    fun check(item: BatchedHubTier): MutableList<String>
}

data class BatchedHubTierSequenceRangeRule(var min: Int, var max: Int) : BatchedHubTierRule {
    override fun check(item: BatchedHubTier): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class BatchedHubTierRequiredCodeRule(var message: String) : BatchedHubTierRule {
    override fun check(item: BatchedHubTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedHubTierCodePrefixRule(var prefix: String) : BatchedHubTierRule {
    override fun check(item: BatchedHubTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedHubTierRequiredDriverIdRule(var message: String) : BatchedHubTierRule {
    override fun check(item: BatchedHubTier): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched hub tier. */
data class BatchedHubTierValidator(var rules: MutableList<BatchedHubTierRule>) {
    fun validate(item: BatchedHubTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedHubTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedHubTier>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedHubTierValidator(): BatchedHubTierValidator {
    return BatchedHubTierValidator(
        mutableListOf<BatchedHubTierRule>(
            BatchedHubTierSequenceRangeRule(64, 1000),
            BatchedHubTierRequiredCodeRule("batched hub tier needs this field"),
            BatchedHubTierCodePrefixRule("ACM"),
            BatchedHubTierRequiredDriverIdRule("batched hub tier needs a value"),
        ),
    )
}
