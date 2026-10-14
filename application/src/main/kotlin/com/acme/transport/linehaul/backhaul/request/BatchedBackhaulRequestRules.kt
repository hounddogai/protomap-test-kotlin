package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.text.normalize

/** A rule a batched backhaul request must pass. Check returns its problems, or none. */
interface BatchedBackhaulRequestRule {
    fun check(item: BatchedBackhaulRequest): MutableList<String>
}

data class BatchedBackhaulRequestCeilingRangeRule(
    var min: Double,
    var max: Double,
) : BatchedBackhaulRequestRule {
    override fun check(item: BatchedBackhaulRequest): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class BatchedBackhaulRequestRequiredLabelRule(var message: String) : BatchedBackhaulRequestRule {
    override fun check(item: BatchedBackhaulRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedBackhaulRequestCodeLengthRule(var maxLength: Int) : BatchedBackhaulRequestRule {
    override fun check(item: BatchedBackhaulRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedBackhaulRequestRequiredCodeRule(var message: String) : BatchedBackhaulRequestRule {
    override fun check(item: BatchedBackhaulRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedBackhaulRequestEndsAtRangeRule(var min: Int, var max: Int) : BatchedBackhaulRequestRule {
    override fun check(item: BatchedBackhaulRequest): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class BatchedBackhaulRequestAllowedRegionRule(
    var allowed: MutableList<String>,
) : BatchedBackhaulRequestRule {
    override fun check(item: BatchedBackhaulRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched backhaul request. */
data class BatchedBackhaulRequestValidator(var rules: MutableList<BatchedBackhaulRequestRule>) {
    fun validate(item: BatchedBackhaulRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedBackhaulRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedBackhaulRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedBackhaulRequestValidator(): BatchedBackhaulRequestValidator {
    return BatchedBackhaulRequestValidator(
        mutableListOf<BatchedBackhaulRequestRule>(
            BatchedBackhaulRequestCeilingRangeRule(5.6, 4889.3),
            BatchedBackhaulRequestRequiredLabelRule("batched backhaul request needs this field"),
            BatchedBackhaulRequestCodeLengthRule(80),
            BatchedBackhaulRequestRequiredCodeRule("batched backhaul request needs this field"),
            BatchedBackhaulRequestEndsAtRangeRule(16, 80),
            BatchedBackhaulRequestAllowedRegionRule(mutableListOf<String>("pending")),
        ),
    )
}
