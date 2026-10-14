package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.text.normalize

/** A rule a batched swap bundle must pass. Check returns its problems, or none. */
interface BatchedSwapBundleRule {
    fun check(item: BatchedSwapBundle): MutableList<String>
}

data class BatchedSwapBundleRequiredCodeRule(var message: String) : BatchedSwapBundleRule {
    override fun check(item: BatchedSwapBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapBundleEndsAtRangeRule(var min: Int, var max: Int) : BatchedSwapBundleRule {
    override fun check(item: BatchedSwapBundle): MutableList<String> {
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

data class BatchedSwapBundleAllowedCategoryRule(var allowed: MutableList<String>) : BatchedSwapBundleRule {
    override fun check(item: BatchedSwapBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapBundleCodePrefixRule(var prefix: String) : BatchedSwapBundleRule {
    override fun check(item: BatchedSwapBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapBundleRequiredNameRule(var message: String) : BatchedSwapBundleRule {
    override fun check(item: BatchedSwapBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapBundleTrailerIdLengthRule(var maxLength: Int) : BatchedSwapBundleRule {
    override fun check(item: BatchedSwapBundle): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched swap bundle. */
data class BatchedSwapBundleValidator(var rules: MutableList<BatchedSwapBundleRule>) {
    fun validate(item: BatchedSwapBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedSwapBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedSwapBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedSwapBundleValidator(): BatchedSwapBundleValidator {
    return BatchedSwapBundleValidator(
        mutableListOf<BatchedSwapBundleRule>(
            BatchedSwapBundleRequiredCodeRule("batched swap bundle needs this field"),
            BatchedSwapBundleEndsAtRangeRule(16, 3600),
            BatchedSwapBundleAllowedCategoryRule(mutableListOf<String>("weighted", "global", "batched")),
            BatchedSwapBundleCodePrefixRule("X"),
            BatchedSwapBundleRequiredNameRule("batched swap bundle needs a value"),
            BatchedSwapBundleTrailerIdLengthRule(255),
        ),
    )
}
