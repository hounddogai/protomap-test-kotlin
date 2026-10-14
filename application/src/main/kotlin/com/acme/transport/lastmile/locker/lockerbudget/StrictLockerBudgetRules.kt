package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.text.normalize

/** A rule a strict locker budget must pass. Check returns its problems, or none. */
interface StrictLockerBudgetRule {
    fun check(item: StrictLockerBudget): MutableList<String>
}

data class StrictLockerBudgetReviewerLengthRule(var maxLength: Int) : StrictLockerBudgetRule {
    override fun check(item: StrictLockerBudget): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictLockerBudgetAllowedNameRule(var allowed: MutableList<String>) : StrictLockerBudgetRule {
    override fun check(item: StrictLockerBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class StrictLockerBudgetAmountRangeRule(var min: Double, var max: Double) : StrictLockerBudgetRule {
    override fun check(item: StrictLockerBudget): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictLockerBudgetCodePrefixRule(var prefix: String) : StrictLockerBudgetRule {
    override fun check(item: StrictLockerBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict locker budget. */
data class StrictLockerBudgetValidator(var rules: MutableList<StrictLockerBudgetRule>) {
    fun validate(item: StrictLockerBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictLockerBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictLockerBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictLockerBudgetValidator(): StrictLockerBudgetValidator {
    return StrictLockerBudgetValidator(
        mutableListOf<StrictLockerBudgetRule>(
            StrictLockerBudgetReviewerLengthRule(16),
            StrictLockerBudgetAllowedNameRule(mutableListOf<String>("manual", "capped")),
            StrictLockerBudgetAmountRangeRule(4.9, 3197.1),
            StrictLockerBudgetCodePrefixRule("REF"),
        ),
    )
}
