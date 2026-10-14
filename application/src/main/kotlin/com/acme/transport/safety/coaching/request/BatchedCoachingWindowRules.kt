package com.acme.transport.safety.coaching.request

import com.acme.transport.kit.text.normalize

/** A rule a batched coaching window must pass. Check returns its problems, or none. */
interface BatchedCoachingWindowRule {
    fun check(item: BatchedCoachingWindow): MutableList<String>
}

data class BatchedCoachingWindowMinimumRangeRule(var min: Double, var max: Double) : BatchedCoachingWindowRule {
    override fun check(item: BatchedCoachingWindow): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class BatchedCoachingWindowRequiredLabelRule(var message: String) : BatchedCoachingWindowRule {
    override fun check(item: BatchedCoachingWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCoachingWindowAllowedLabelRule(var allowed: MutableList<String>) : BatchedCoachingWindowRule {
    override fun check(item: BatchedCoachingWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched coaching window. */
data class BatchedCoachingWindowValidator(var rules: MutableList<BatchedCoachingWindowRule>) {
    fun validate(item: BatchedCoachingWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCoachingWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCoachingWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCoachingWindowValidator(): BatchedCoachingWindowValidator {
    return BatchedCoachingWindowValidator(
        mutableListOf<BatchedCoachingWindowRule>(
            BatchedCoachingWindowMinimumRangeRule(2.7, 2256.7),
            BatchedCoachingWindowRequiredLabelRule("batched coaching window needs an entry"),
            BatchedCoachingWindowAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
