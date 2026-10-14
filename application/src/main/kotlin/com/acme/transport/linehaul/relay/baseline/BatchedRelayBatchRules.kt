package com.acme.transport.linehaul.relay.baseline

/** A rule a batched relay batch must pass. Check returns its problems, or none. */
interface BatchedRelayBatchRule {
    fun check(item: BatchedRelayBatch): MutableList<String>
}

data class BatchedRelayBatchContactEmailLengthRule(var maxLength: Int) : BatchedRelayBatchRule {
    override fun check(item: BatchedRelayBatch): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayBatchRequiredCodeRule(var message: String) : BatchedRelayBatchRule {
    override fun check(item: BatchedRelayBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayBatchActiveGuardRule(var limit: Double) : BatchedRelayBatchRule {
    override fun check(item: BatchedRelayBatch): MutableList<String> {
        if (item.active && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayBatchRatioRangeRule(var min: Double, var max: Double) : BatchedRelayBatchRule {
    override fun check(item: BatchedRelayBatch): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayBatchCodeLengthRule(var maxLength: Int) : BatchedRelayBatchRule {
    override fun check(item: BatchedRelayBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched relay batch. */
data class BatchedRelayBatchValidator(var rules: MutableList<BatchedRelayBatchRule>) {
    fun validate(item: BatchedRelayBatch): MutableList<String> {
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

    fun requireValid(item: BatchedRelayBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRelayBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRelayBatchValidator(): BatchedRelayBatchValidator {
    return BatchedRelayBatchValidator(
        mutableListOf<BatchedRelayBatchRule>(
            BatchedRelayBatchContactEmailLengthRule(80),
            BatchedRelayBatchRequiredCodeRule("batched relay batch needs a value"),
            BatchedRelayBatchActiveGuardRule(8.6),
            BatchedRelayBatchRatioRangeRule(0.3, 898.5),
            BatchedRelayBatchCodeLengthRule(255),
        ),
    )
}
