package com.acme.transport.customs.clearance.clearancecheckpoint

/** A rule a batched clearance checkpoint must pass. Check returns its problems, or none. */
interface BatchedClearanceCheckpointRule {
    fun check(item: BatchedClearanceCheckpoint): MutableList<String>
}

data class BatchedClearanceCheckpointCodeLengthRule(var maxLength: Int) : BatchedClearanceCheckpointRule {
    override fun check(item: BatchedClearanceCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedClearanceCheckpointRequiredDepotRule(var message: String) : BatchedClearanceCheckpointRule {
    override fun check(item: BatchedClearanceCheckpoint): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedClearanceCheckpointShiftCodeLengthRule(var maxLength: Int) : BatchedClearanceCheckpointRule {
    override fun check(item: BatchedClearanceCheckpoint): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedClearanceCheckpointAmountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedClearanceCheckpointRule {
    override fun check(item: BatchedClearanceCheckpoint): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched clearance checkpoint. */
data class BatchedClearanceCheckpointValidator(var rules: MutableList<BatchedClearanceCheckpointRule>) {
    fun validate(item: BatchedClearanceCheckpoint): MutableList<String> {
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

    fun requireValid(item: BatchedClearanceCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedClearanceCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedClearanceCheckpointValidator(): BatchedClearanceCheckpointValidator {
    return BatchedClearanceCheckpointValidator(
        mutableListOf<BatchedClearanceCheckpointRule>(
            BatchedClearanceCheckpointCodeLengthRule(1000),
            BatchedClearanceCheckpointRequiredDepotRule("batched clearance checkpoint needs this field"),
            BatchedClearanceCheckpointShiftCodeLengthRule(80),
            BatchedClearanceCheckpointAmountRangeRule(7.1, 4243.6),
        ),
    )
}
