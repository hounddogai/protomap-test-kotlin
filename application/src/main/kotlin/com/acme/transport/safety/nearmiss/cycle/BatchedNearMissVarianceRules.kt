package com.acme.transport.safety.nearmiss.cycle

import com.acme.transport.kit.text.normalize

/** A rule a batched near miss variance must pass. Check returns its problems, or none. */
interface BatchedNearMissVarianceRule {
    fun check(item: BatchedNearMissVariance): MutableList<String>
}

data class BatchedNearMissVarianceCodeLengthRule(var maxLength: Int) : BatchedNearMissVarianceRule {
    override fun check(item: BatchedNearMissVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissVarianceNotesLengthRule(var maxLength: Int) : BatchedNearMissVarianceRule {
    override fun check(item: BatchedNearMissVariance): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissVarianceReviewerLengthRule(var maxLength: Int) : BatchedNearMissVarianceRule {
    override fun check(item: BatchedNearMissVariance): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissVarianceBudgetRangeRule(
    var min: Double,
    var max: Double,
) : BatchedNearMissVarianceRule {
    override fun check(item: BatchedNearMissVariance): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class BatchedNearMissVarianceAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedNearMissVarianceRule {
    override fun check(item: BatchedNearMissVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched near miss variance. */
data class BatchedNearMissVarianceValidator(var rules: MutableList<BatchedNearMissVarianceRule>) {
    fun validate(item: BatchedNearMissVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedNearMissVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNearMissVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNearMissVarianceValidator(): BatchedNearMissVarianceValidator {
    return BatchedNearMissVarianceValidator(
        mutableListOf<BatchedNearMissVarianceRule>(
            BatchedNearMissVarianceCodeLengthRule(120),
            BatchedNearMissVarianceNotesLengthRule(120),
            BatchedNearMissVarianceReviewerLengthRule(1000),
            BatchedNearMissVarianceBudgetRangeRule(4.8, 2049.7),
            BatchedNearMissVarianceAllowedCodeRule(mutableListOf<String>("strict", "seasonal", "batched")),
        ),
    )
}
