package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.normalize

/** A rule a batched coaching digest must pass. Check returns its problems, or none. */
interface BatchedCoachingDigestRule {
    fun check(item: BatchedCoachingDigest): MutableList<String>
}

data class BatchedCoachingDigestRequiredNotesRule(var message: String) : BatchedCoachingDigestRule {
    override fun check(item: BatchedCoachingDigest): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCoachingDigestAllowedCodeRule(var allowed: MutableList<String>) : BatchedCoachingDigestRule {
    override fun check(item: BatchedCoachingDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCoachingDigestStartsAtRangeRule(var min: Int, var max: Int) : BatchedCoachingDigestRule {
    override fun check(item: BatchedCoachingDigest): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched coaching digest. */
data class BatchedCoachingDigestValidator(var rules: MutableList<BatchedCoachingDigestRule>) {
    fun validate(item: BatchedCoachingDigest): MutableList<String> {
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

    fun requireValid(item: BatchedCoachingDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCoachingDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCoachingDigestValidator(): BatchedCoachingDigestValidator {
    return BatchedCoachingDigestValidator(
        mutableListOf<BatchedCoachingDigestRule>(
            BatchedCoachingDigestRequiredNotesRule("batched coaching digest needs this field"),
            BatchedCoachingDigestAllowedCodeRule(mutableListOf<String>()),
            BatchedCoachingDigestStartsAtRangeRule(3600, 86400),
        ),
    )
}
