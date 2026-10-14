package com.acme.transport.crewing.swap.slot

/** A rule a swap batch must pass. Check returns its problems, or none. */
interface SwapBatchRule {
    fun check(item: SwapBatch): MutableList<String>
}

data class SwapBatchVersionRangeRule(var min: Int, var max: Int) : SwapBatchRule {
    override fun check(item: SwapBatch): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapBatchLocaleLengthRule(var maxLength: Int) : SwapBatchRule {
    override fun check(item: SwapBatch): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapBatchLabelLengthRule(var maxLength: Int) : SwapBatchRule {
    override fun check(item: SwapBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapBatchUpdatedAtRangeRule(var min: Int, var max: Int) : SwapBatchRule {
    override fun check(item: SwapBatch): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap batch. */
data class SwapBatchValidator(var rules: MutableList<SwapBatchRule>) {
    fun validate(item: SwapBatch): MutableList<String> {
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

    fun requireValid(item: SwapBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapBatchValidator(): SwapBatchValidator {
    return SwapBatchValidator(
        mutableListOf<SwapBatchRule>(
            SwapBatchVersionRangeRule(1000, 1000),
            SwapBatchLocaleLengthRule(16),
            SwapBatchLabelLengthRule(32),
            SwapBatchUpdatedAtRangeRule(120, 255),
        ),
    )
}
