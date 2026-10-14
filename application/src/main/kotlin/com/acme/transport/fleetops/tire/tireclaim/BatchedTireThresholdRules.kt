package com.acme.transport.fleetops.tire.tireclaim

/** A rule a batched tire threshold must pass. Check returns its problems, or none. */
interface BatchedTireThresholdRule {
    fun check(item: BatchedTireThreshold): MutableList<String>
}

data class BatchedTireThresholdGraceDaysRangeRule(var min: Int, var max: Int) : BatchedTireThresholdRule {
    override fun check(item: BatchedTireThreshold): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTireThresholdRequiredCodeRule(var message: String) : BatchedTireThresholdRule {
    override fun check(item: BatchedTireThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTireThresholdCodePrefixRule(var prefix: String) : BatchedTireThresholdRule {
    override fun check(item: BatchedTireThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTireThresholdTitleLengthRule(var maxLength: Int) : BatchedTireThresholdRule {
    override fun check(item: BatchedTireThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tire threshold. */
data class BatchedTireThresholdValidator(var rules: MutableList<BatchedTireThresholdRule>) {
    fun validate(item: BatchedTireThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTireThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTireThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTireThresholdValidator(): BatchedTireThresholdValidator {
    return BatchedTireThresholdValidator(
        mutableListOf<BatchedTireThresholdRule>(
            BatchedTireThresholdGraceDaysRangeRule(255, 1000),
            BatchedTireThresholdRequiredCodeRule("batched tire threshold needs a value"),
            BatchedTireThresholdCodePrefixRule("ACM"),
            BatchedTireThresholdTitleLengthRule(64),
        ),
    )
}
