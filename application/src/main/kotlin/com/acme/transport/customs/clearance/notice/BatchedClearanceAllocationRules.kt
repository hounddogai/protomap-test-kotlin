package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.text.normalize

/** A rule a batched clearance allocation must pass. Check returns its problems, or none. */
interface BatchedClearanceAllocationRule {
    fun check(item: BatchedClearanceAllocation): MutableList<String>
}

data class BatchedClearanceAllocationAllowedDockRule(
    var allowed: MutableList<String>,
) : BatchedClearanceAllocationRule {
    override fun check(item: BatchedClearanceAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class BatchedClearanceAllocationTitleLengthRule(var maxLength: Int) : BatchedClearanceAllocationRule {
    override fun check(item: BatchedClearanceAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedClearanceAllocationRequiredDockRule(var message: String) : BatchedClearanceAllocationRule {
    override fun check(item: BatchedClearanceAllocation): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedClearanceAllocationCodeLengthRule(var maxLength: Int) : BatchedClearanceAllocationRule {
    override fun check(item: BatchedClearanceAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedClearanceAllocationRequiredLocaleRule(var message: String) : BatchedClearanceAllocationRule {
    override fun check(item: BatchedClearanceAllocation): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedClearanceAllocationBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : BatchedClearanceAllocationRule {
    override fun check(item: BatchedClearanceAllocation): MutableList<String> {
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

/** Runs every rule of a batched clearance allocation. */
data class BatchedClearanceAllocationValidator(var rules: MutableList<BatchedClearanceAllocationRule>) {
    fun validate(item: BatchedClearanceAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedClearanceAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedClearanceAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedClearanceAllocationValidator(): BatchedClearanceAllocationValidator {
    return BatchedClearanceAllocationValidator(
        mutableListOf<BatchedClearanceAllocationRule>(
            BatchedClearanceAllocationAllowedDockRule(mutableListOf<String>()),
            BatchedClearanceAllocationTitleLengthRule(16),
            BatchedClearanceAllocationRequiredDockRule("batched clearance allocation needs an entry"),
            BatchedClearanceAllocationCodeLengthRule(80),
            BatchedClearanceAllocationRequiredLocaleRule("batched clearance allocation needs this field"),
            BatchedClearanceAllocationBatchSizeRangeRule(80, 255),
        ),
    )
}
