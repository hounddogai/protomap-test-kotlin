package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.text.normalize

/** A rule a batched cross dock variance must pass. Check returns its problems, or none. */
interface BatchedCrossDockVarianceRule {
    fun check(item: BatchedCrossDockVariance): MutableList<String>
}

data class BatchedCrossDockVarianceSegmentLengthRule(var maxLength: Int) : BatchedCrossDockVarianceRule {
    override fun check(item: BatchedCrossDockVariance): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockVarianceAllowedCategoryRule(
    var allowed: MutableList<String>,
) : BatchedCrossDockVarianceRule {
    override fun check(item: BatchedCrossDockVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is missing")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockVarianceCodePrefixRule(var prefix: String) : BatchedCrossDockVarianceRule {
    override fun check(item: BatchedCrossDockVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockVarianceCodeLengthRule(var maxLength: Int) : BatchedCrossDockVarianceRule {
    override fun check(item: BatchedCrossDockVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockVarianceRequiredCodeRule(var message: String) : BatchedCrossDockVarianceRule {
    override fun check(item: BatchedCrossDockVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockVarianceTitleLengthRule(var maxLength: Int) : BatchedCrossDockVarianceRule {
    override fun check(item: BatchedCrossDockVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched cross dock variance. */
data class BatchedCrossDockVarianceValidator(var rules: MutableList<BatchedCrossDockVarianceRule>) {
    fun validate(item: BatchedCrossDockVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCrossDockVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCrossDockVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCrossDockVarianceValidator(): BatchedCrossDockVarianceValidator {
    return BatchedCrossDockVarianceValidator(
        mutableListOf<BatchedCrossDockVarianceRule>(
            BatchedCrossDockVarianceSegmentLengthRule(80),
            BatchedCrossDockVarianceAllowedCategoryRule(mutableListOf<String>("fallback")),
            BatchedCrossDockVarianceCodePrefixRule("Q"),
            BatchedCrossDockVarianceCodeLengthRule(80),
            BatchedCrossDockVarianceRequiredCodeRule("batched cross dock variance needs this field"),
            BatchedCrossDockVarianceTitleLengthRule(80),
        ),
    )
}
