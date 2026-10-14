package com.acme.transport.customs.tariff.tariffoverride

/** A rule a batched tariff checkpoint must pass. Check returns its problems, or none. */
interface BatchedTariffCheckpointRule {
    fun check(item: BatchedTariffCheckpoint): MutableList<String>
}

data class BatchedTariffCheckpointSequenceRangeRule(var min: Int, var max: Int) : BatchedTariffCheckpointRule {
    override fun check(item: BatchedTariffCheckpoint): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffCheckpointCodeLengthRule(var maxLength: Int) : BatchedTariffCheckpointRule {
    override fun check(item: BatchedTariffCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffCheckpointStartsAtRangeRule(var min: Int, var max: Int) : BatchedTariffCheckpointRule {
    override fun check(item: BatchedTariffCheckpoint): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tariff checkpoint. */
data class BatchedTariffCheckpointValidator(var rules: MutableList<BatchedTariffCheckpointRule>) {
    fun validate(item: BatchedTariffCheckpoint): MutableList<String> {
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

    fun requireValid(item: BatchedTariffCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTariffCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTariffCheckpointValidator(): BatchedTariffCheckpointValidator {
    return BatchedTariffCheckpointValidator(
        mutableListOf<BatchedTariffCheckpointRule>(
            BatchedTariffCheckpointSequenceRangeRule(32, 80),
            BatchedTariffCheckpointCodeLengthRule(120),
            BatchedTariffCheckpointStartsAtRangeRule(1000, 3600),
        ),
    )
}
