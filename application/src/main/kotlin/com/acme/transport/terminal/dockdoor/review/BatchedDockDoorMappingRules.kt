package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.text.normalize

/** A rule a batched dock door mapping must pass. Check returns its problems, or none. */
interface BatchedDockDoorMappingRule {
    fun check(item: BatchedDockDoorMapping): MutableList<String>
}

data class BatchedDockDoorMappingTitleLengthRule(var maxLength: Int) : BatchedDockDoorMappingRule {
    override fun check(item: BatchedDockDoorMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorMappingRequiredSourceRule(var message: String) : BatchedDockDoorMappingRule {
    override fun check(item: BatchedDockDoorMapping): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorMappingAllowedTitleRule(
    var allowed: MutableList<String>,
) : BatchedDockDoorMappingRule {
    override fun check(item: BatchedDockDoorMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorMappingRateRangeRule(var min: Double, var max: Double) : BatchedDockDoorMappingRule {
    override fun check(item: BatchedDockDoorMapping): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dock door mapping. */
data class BatchedDockDoorMappingValidator(var rules: MutableList<BatchedDockDoorMappingRule>) {
    fun validate(item: BatchedDockDoorMapping): MutableList<String> {
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

    fun requireValid(item: BatchedDockDoorMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDockDoorMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDockDoorMappingValidator(): BatchedDockDoorMappingValidator {
    return BatchedDockDoorMappingValidator(
        mutableListOf<BatchedDockDoorMappingRule>(
            BatchedDockDoorMappingTitleLengthRule(16),
            BatchedDockDoorMappingRequiredSourceRule("batched dock door mapping needs this field"),
            BatchedDockDoorMappingAllowedTitleRule(mutableListOf<String>("rolling", "default", "strict")),
            BatchedDockDoorMappingRateRangeRule(8.9, 880.8),
        ),
    )
}
