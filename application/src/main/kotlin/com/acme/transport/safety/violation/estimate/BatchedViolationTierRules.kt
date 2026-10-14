package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.text.normalize

/** A rule a batched violation tier must pass. Check returns its problems, or none. */
interface BatchedViolationTierRule {
    fun check(item: BatchedViolationTier): MutableList<String>
}

data class BatchedViolationTierRequiredCategoryRule(var message: String) : BatchedViolationTierRule {
    override fun check(item: BatchedViolationTier): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationTierAllowedPhoneRule(var allowed: MutableList<String>) : BatchedViolationTierRule {
    override fun check(item: BatchedViolationTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationTierLabelLengthRule(var maxLength: Int) : BatchedViolationTierRule {
    override fun check(item: BatchedViolationTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationTierLoadKgRangeRule(var min: Double, var max: Double) : BatchedViolationTierRule {
    override fun check(item: BatchedViolationTier): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class BatchedViolationTierCodePrefixRule(var prefix: String) : BatchedViolationTierRule {
    override fun check(item: BatchedViolationTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched violation tier. */
data class BatchedViolationTierValidator(var rules: MutableList<BatchedViolationTierRule>) {
    fun validate(item: BatchedViolationTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedViolationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedViolationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedViolationTierValidator(): BatchedViolationTierValidator {
    return BatchedViolationTierValidator(
        mutableListOf<BatchedViolationTierRule>(
            BatchedViolationTierRequiredCategoryRule("batched violation tier needs this field"),
            BatchedViolationTierAllowedPhoneRule(mutableListOf<String>("legacy", "strict", "global")),
            BatchedViolationTierLabelLengthRule(80),
            BatchedViolationTierLoadKgRangeRule(0.2, 3462.7),
            BatchedViolationTierCodePrefixRule("X"),
        ),
    )
}
