package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.text.normalize

/** A rule a fallback coaching budget must pass. Check returns its problems, or none. */
interface FallbackCoachingBudgetRule {
    fun check(item: FallbackCoachingBudget): MutableList<String>
}

data class FallbackCoachingBudgetMinimumRangeRule(
    var min: Double,
    var max: Double,
) : FallbackCoachingBudgetRule {
    override fun check(item: FallbackCoachingBudget): MutableList<String> {
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

data class FallbackCoachingBudgetAllowedLabelRule(
    var allowed: MutableList<String>,
) : FallbackCoachingBudgetRule {
    override fun check(item: FallbackCoachingBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCoachingBudgetCodeLengthRule(var maxLength: Int) : FallbackCoachingBudgetRule {
    override fun check(item: FallbackCoachingBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback coaching budget. */
data class FallbackCoachingBudgetValidator(var rules: MutableList<FallbackCoachingBudgetRule>) {
    fun validate(item: FallbackCoachingBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackCoachingBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCoachingBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCoachingBudgetValidator(): FallbackCoachingBudgetValidator {
    return FallbackCoachingBudgetValidator(
        mutableListOf<FallbackCoachingBudgetRule>(
            FallbackCoachingBudgetMinimumRangeRule(1.5, 68.7),
            FallbackCoachingBudgetAllowedLabelRule(mutableListOf<String>("batched", "weighted")),
            FallbackCoachingBudgetCodeLengthRule(86400),
        ),
    )
}
