package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

/** A rule a batched accessorial checkpoint must pass. Check returns its problems, or none. */
interface BatchedAccessorialCheckpointRule {
    fun check(item: BatchedAccessorialCheckpoint): MutableList<String>
}

data class BatchedAccessorialCheckpointCostRangeRule(
    var min: Double,
    var max: Double,
) : BatchedAccessorialCheckpointRule {
    override fun check(item: BatchedAccessorialCheckpoint): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialCheckpointCodePrefixRule(var prefix: String) : BatchedAccessorialCheckpointRule {
    override fun check(item: BatchedAccessorialCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialCheckpointDockLengthRule(var maxLength: Int) : BatchedAccessorialCheckpointRule {
    override fun check(item: BatchedAccessorialCheckpoint): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialCheckpointRequiredDockRule(
    var message: String,
) : BatchedAccessorialCheckpointRule {
    override fun check(item: BatchedAccessorialCheckpoint): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched accessorial checkpoint. */
data class BatchedAccessorialCheckpointValidator(var rules: MutableList<BatchedAccessorialCheckpointRule>) {
    fun validate(item: BatchedAccessorialCheckpoint): MutableList<String> {
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

    fun requireValid(item: BatchedAccessorialCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAccessorialCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAccessorialCheckpointValidator(): BatchedAccessorialCheckpointValidator {
    return BatchedAccessorialCheckpointValidator(
        mutableListOf<BatchedAccessorialCheckpointRule>(
            BatchedAccessorialCheckpointCostRangeRule(0.6, 1869.9),
            BatchedAccessorialCheckpointCodePrefixRule("X"),
            BatchedAccessorialCheckpointDockLengthRule(3600),
            BatchedAccessorialCheckpointRequiredDockRule("batched accessorial checkpoint needs a value"),
        ),
    )
}
