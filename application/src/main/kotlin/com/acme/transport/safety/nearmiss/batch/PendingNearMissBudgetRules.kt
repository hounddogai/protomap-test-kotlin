package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.normalize

/** A rule a pending near miss budget must pass. Check returns its problems, or none. */
interface PendingNearMissBudgetRule {
    fun check(item: PendingNearMissBudget): MutableList<String>
}

data class PendingNearMissBudgetBatchSizeRangeRule(var min: Int, var max: Int) : PendingNearMissBudgetRule {
    override fun check(item: PendingNearMissBudget): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissBudgetExpiresAtRangeRule(var min: Int, var max: Int) : PendingNearMissBudgetRule {
    override fun check(item: PendingNearMissBudget): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class PendingNearMissBudgetBudgetRangeRule(var min: Double, var max: Double) : PendingNearMissBudgetRule {
    override fun check(item: PendingNearMissBudget): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissBudgetAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : PendingNearMissBudgetRule {
    override fun check(item: PendingNearMissBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending near miss budget. */
data class PendingNearMissBudgetValidator(var rules: MutableList<PendingNearMissBudgetRule>) {
    fun validate(item: PendingNearMissBudget): MutableList<String> {
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

    fun requireValid(item: PendingNearMissBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNearMissBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNearMissBudgetValidator(): PendingNearMissBudgetValidator {
    return PendingNearMissBudgetValidator(
        mutableListOf<PendingNearMissBudgetRule>(
            PendingNearMissBudgetBatchSizeRangeRule(32, 86400),
            PendingNearMissBudgetExpiresAtRangeRule(120, 3600),
            PendingNearMissBudgetBudgetRangeRule(3.2, 372.6),
            PendingNearMissBudgetAllowedTrailerIdRule(mutableListOf<String>()),
        ),
    )
}
