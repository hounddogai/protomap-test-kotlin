package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.normalize

/** A rule a batched capacity batch must pass. Check returns its problems, or none. */
interface BatchedCapacityBatchRule {
    fun check(item: BatchedCapacityBatch): MutableList<String>
}

data class BatchedCapacityBatchSummaryLengthRule(var maxLength: Int) : BatchedCapacityBatchRule {
    override fun check(item: BatchedCapacityBatch): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityBatchFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCapacityBatchRule {
    override fun check(item: BatchedCapacityBatch): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityBatchAllowedCodeRule(var allowed: MutableList<String>) : BatchedCapacityBatchRule {
    override fun check(item: BatchedCapacityBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityBatchPalletsRangeRule(var min: Int, var max: Int) : BatchedCapacityBatchRule {
    override fun check(item: BatchedCapacityBatch): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityBatchFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCapacityBatchRule {
    override fun check(item: BatchedCapacityBatch): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityBatchVersionRangeRule(var min: Int, var max: Int) : BatchedCapacityBatchRule {
    override fun check(item: BatchedCapacityBatch): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched capacity batch. */
data class BatchedCapacityBatchValidator(var rules: MutableList<BatchedCapacityBatchRule>) {
    fun validate(item: BatchedCapacityBatch): MutableList<String> {
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

    fun requireValid(item: BatchedCapacityBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCapacityBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCapacityBatchValidator(): BatchedCapacityBatchValidator {
    return BatchedCapacityBatchValidator(
        mutableListOf<BatchedCapacityBatchRule>(
            BatchedCapacityBatchSummaryLengthRule(16),
            BatchedCapacityBatchFuelLitersRangeRule(0.1, 163.9),
            BatchedCapacityBatchAllowedCodeRule(mutableListOf<String>()),
            BatchedCapacityBatchPalletsRangeRule(32, 3600),
            BatchedCapacityBatchFloorValueRangeRule(1.1, 1913.7),
            BatchedCapacityBatchVersionRangeRule(86400, 86400),
        ),
    )
}
