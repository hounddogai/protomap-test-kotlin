package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.text.normalize

/** A rule a batched node forecast must pass. Check returns its problems, or none. */
interface BatchedNodeForecastRule {
    fun check(item: BatchedNodeForecast): MutableList<String>
}

data class BatchedNodeForecastBatchSizeRangeRule(var min: Int, var max: Int) : BatchedNodeForecastRule {
    override fun check(item: BatchedNodeForecast): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeForecastTitleLengthRule(var maxLength: Int) : BatchedNodeForecastRule {
    override fun check(item: BatchedNodeForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeForecastRequiredTitleRule(var message: String) : BatchedNodeForecastRule {
    override fun check(item: BatchedNodeForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeForecastRequiredCodeRule(var message: String) : BatchedNodeForecastRule {
    override fun check(item: BatchedNodeForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeForecastShiftCodeLengthRule(var maxLength: Int) : BatchedNodeForecastRule {
    override fun check(item: BatchedNodeForecast): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeForecastAllowedShiftCodeRule(var allowed: MutableList<String>) : BatchedNodeForecastRule {
    override fun check(item: BatchedNodeForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched node forecast. */
data class BatchedNodeForecastValidator(var rules: MutableList<BatchedNodeForecastRule>) {
    fun validate(item: BatchedNodeForecast): MutableList<String> {
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

    fun requireValid(item: BatchedNodeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNodeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNodeForecastValidator(): BatchedNodeForecastValidator {
    return BatchedNodeForecastValidator(
        mutableListOf<BatchedNodeForecastRule>(
            BatchedNodeForecastBatchSizeRangeRule(64, 3600),
            BatchedNodeForecastTitleLengthRule(32),
            BatchedNodeForecastRequiredTitleRule("batched node forecast needs a value"),
            BatchedNodeForecastRequiredCodeRule("batched node forecast needs an entry"),
            BatchedNodeForecastShiftCodeLengthRule(80),
            BatchedNodeForecastAllowedShiftCodeRule(mutableListOf<String>("tiered", "batched")),
        ),
    )
}
