package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.kit.text.normalize

/** A rule a batched defect mapping must pass. Check returns its problems, or none. */
interface BatchedDefectMappingRule {
    fun check(item: BatchedDefectMapping): MutableList<String>
}

data class BatchedDefectMappingWindowDaysRangeRule(var min: Int, var max: Int) : BatchedDefectMappingRule {
    override fun check(item: BatchedDefectMapping): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectMappingCodeLengthRule(var maxLength: Int) : BatchedDefectMappingRule {
    override fun check(item: BatchedDefectMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectMappingAllowedCodeRule(var allowed: MutableList<String>) : BatchedDefectMappingRule {
    override fun check(item: BatchedDefectMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectMappingRequiredReviewerRule(var message: String) : BatchedDefectMappingRule {
    override fun check(item: BatchedDefectMapping): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched defect mapping. */
data class BatchedDefectMappingValidator(var rules: MutableList<BatchedDefectMappingRule>) {
    fun validate(item: BatchedDefectMapping): MutableList<String> {
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

    fun requireValid(item: BatchedDefectMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDefectMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDefectMappingValidator(): BatchedDefectMappingValidator {
    return BatchedDefectMappingValidator(
        mutableListOf<BatchedDefectMappingRule>(
            BatchedDefectMappingWindowDaysRangeRule(32, 86400),
            BatchedDefectMappingCodeLengthRule(120),
            BatchedDefectMappingAllowedCodeRule(mutableListOf<String>("rolling", "pending", "rolling")),
            BatchedDefectMappingRequiredReviewerRule("batched defect mapping needs a value"),
        ),
    )
}
