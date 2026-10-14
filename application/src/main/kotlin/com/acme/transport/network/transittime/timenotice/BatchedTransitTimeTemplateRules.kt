package com.acme.transport.network.transittime.timenotice

/** A rule a batched transit time template must pass. Check returns its problems, or none. */
interface BatchedTransitTimeTemplateRule {
    fun check(item: BatchedTransitTimeTemplate): MutableList<String>
}

data class BatchedTransitTimeTemplateRatioRangeRule(
    var min: Double,
    var max: Double,
) : BatchedTransitTimeTemplateRule {
    override fun check(item: BatchedTransitTimeTemplate): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTransitTimeTemplateCodeLengthRule(var maxLength: Int) : BatchedTransitTimeTemplateRule {
    override fun check(item: BatchedTransitTimeTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTransitTimeTemplateBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : BatchedTransitTimeTemplateRule {
    override fun check(item: BatchedTransitTimeTemplate): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched transit time template. */
data class BatchedTransitTimeTemplateValidator(var rules: MutableList<BatchedTransitTimeTemplateRule>) {
    fun validate(item: BatchedTransitTimeTemplate): MutableList<String> {
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

    fun requireValid(item: BatchedTransitTimeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTransitTimeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTransitTimeTemplateValidator(): BatchedTransitTimeTemplateValidator {
    return BatchedTransitTimeTemplateValidator(
        mutableListOf<BatchedTransitTimeTemplateRule>(
            BatchedTransitTimeTemplateRatioRangeRule(4.7, 3818.7),
            BatchedTransitTimeTemplateCodeLengthRule(120),
            BatchedTransitTimeTemplateBatchSizeRangeRule(16, 80),
        ),
    )
}
