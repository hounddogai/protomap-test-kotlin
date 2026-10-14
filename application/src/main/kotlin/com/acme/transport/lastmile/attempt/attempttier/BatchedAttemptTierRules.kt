package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.kit.text.normalize

/** A rule a batched attempt tier must pass. Check returns its problems, or none. */
interface BatchedAttemptTierRule {
    fun check(item: BatchedAttemptTier): MutableList<String>
}

data class BatchedAttemptTierEndsAtRangeRule(var min: Int, var max: Int) : BatchedAttemptTierRule {
    override fun check(item: BatchedAttemptTier): MutableList<String> {
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

data class BatchedAttemptTierCodeLengthRule(var maxLength: Int) : BatchedAttemptTierRule {
    override fun check(item: BatchedAttemptTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAttemptTierAllowedTrailerIdRule(var allowed: MutableList<String>) : BatchedAttemptTierRule {
    override fun check(item: BatchedAttemptTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAttemptTierArchivedGuardRule(var limit: Int) : BatchedAttemptTierRule {
    override fun check(item: BatchedAttemptTier): MutableList<String> {
        if (item.archived && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAttemptTierFuelLitersRangeRule(var min: Double, var max: Double) : BatchedAttemptTierRule {
    override fun check(item: BatchedAttemptTier): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched attempt tier. */
data class BatchedAttemptTierValidator(var rules: MutableList<BatchedAttemptTierRule>) {
    fun validate(item: BatchedAttemptTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAttemptTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAttemptTier>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAttemptTierValidator(): BatchedAttemptTierValidator {
    return BatchedAttemptTierValidator(
        mutableListOf<BatchedAttemptTierRule>(
            BatchedAttemptTierEndsAtRangeRule(255, 1000),
            BatchedAttemptTierCodeLengthRule(1000),
            BatchedAttemptTierAllowedTrailerIdRule(mutableListOf<String>("adaptive", "lenient", "tiered")),
            BatchedAttemptTierArchivedGuardRule(80),
            BatchedAttemptTierFuelLitersRangeRule(8.7, 192.1),
        ),
    )
}
