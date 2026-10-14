package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.text.normalize

/** A rule a batched trailer score must pass. Check returns its problems, or none. */
interface BatchedTrailerScoreRule {
    fun check(item: BatchedTrailerScore): MutableList<String>
}

data class BatchedTrailerScoreLabelLengthRule(var maxLength: Int) : BatchedTrailerScoreRule {
    override fun check(item: BatchedTrailerScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerScoreWindowDaysRangeRule(var min: Int, var max: Int) : BatchedTrailerScoreRule {
    override fun check(item: BatchedTrailerScore): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerScoreCostRangeRule(var min: Double, var max: Double) : BatchedTrailerScoreRule {
    override fun check(item: BatchedTrailerScore): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerScoreAllowedCodeRule(var allowed: MutableList<String>) : BatchedTrailerScoreRule {
    override fun check(item: BatchedTrailerScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer score. */
data class BatchedTrailerScoreValidator(var rules: MutableList<BatchedTrailerScoreRule>) {
    fun validate(item: BatchedTrailerScore): MutableList<String> {
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

    fun requireValid(item: BatchedTrailerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerScoreValidator(): BatchedTrailerScoreValidator {
    return BatchedTrailerScoreValidator(
        mutableListOf<BatchedTrailerScoreRule>(
            BatchedTrailerScoreLabelLengthRule(1000),
            BatchedTrailerScoreWindowDaysRangeRule(64, 80),
            BatchedTrailerScoreCostRangeRule(0.4, 4698.8),
            BatchedTrailerScoreAllowedCodeRule(mutableListOf<String>("global")),
        ),
    )
}
