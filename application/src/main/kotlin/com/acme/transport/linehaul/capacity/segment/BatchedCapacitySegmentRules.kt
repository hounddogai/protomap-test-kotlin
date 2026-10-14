package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.text.normalize

/** A rule a batched capacity segment must pass. Check returns its problems, or none. */
interface BatchedCapacitySegmentRule {
    fun check(item: BatchedCapacitySegment): MutableList<String>
}

data class BatchedCapacitySegmentCodePrefixRule(var prefix: String) : BatchedCapacitySegmentRule {
    override fun check(item: BatchedCapacitySegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacitySegmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedCapacitySegmentRule {
    override fun check(item: BatchedCapacitySegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacitySegmentSummaryLengthRule(var maxLength: Int) : BatchedCapacitySegmentRule {
    override fun check(item: BatchedCapacitySegment): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacitySegmentNameLengthRule(var maxLength: Int) : BatchedCapacitySegmentRule {
    override fun check(item: BatchedCapacitySegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacitySegmentRequiredNameRule(var message: String) : BatchedCapacitySegmentRule {
    override fun check(item: BatchedCapacitySegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched capacity segment. */
data class BatchedCapacitySegmentValidator(var rules: MutableList<BatchedCapacitySegmentRule>) {
    fun validate(item: BatchedCapacitySegment): MutableList<String> {
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

    fun requireValid(item: BatchedCapacitySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCapacitySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCapacitySegmentValidator(): BatchedCapacitySegmentValidator {
    return BatchedCapacitySegmentValidator(
        mutableListOf<BatchedCapacitySegmentRule>(
            BatchedCapacitySegmentCodePrefixRule("ACM"),
            BatchedCapacitySegmentAllowedCodeRule(mutableListOf<String>("fallback", "manual")),
            BatchedCapacitySegmentSummaryLengthRule(255),
            BatchedCapacitySegmentNameLengthRule(3600),
            BatchedCapacitySegmentRequiredNameRule("batched capacity segment needs this field"),
        ),
    )
}
