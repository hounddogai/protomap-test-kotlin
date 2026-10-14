package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.text.normalize

/** A rule a coaching budget must pass. Check returns its problems, or none. */
interface CoachingBudgetRule {
    fun check(item: CoachingBudget): MutableList<String>
}

data class CoachingBudgetRequiredLabelRule(var message: String) : CoachingBudgetRule {
    override fun check(item: CoachingBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingBudgetRequiredCodeRule(var message: String) : CoachingBudgetRule {
    override fun check(item: CoachingBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingBudgetCodeLengthRule(var maxLength: Int) : CoachingBudgetRule {
    override fun check(item: CoachingBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingBudgetAllowedLocaleRule(var allowed: MutableList<String>) : CoachingBudgetRule {
    override fun check(item: CoachingBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CoachingBudgetExpiresAtRangeRule(var min: Int, var max: Int) : CoachingBudgetRule {
    override fun check(item: CoachingBudget): MutableList<String> {
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

/** Runs every rule of a coaching budget. */
data class CoachingBudgetValidator(var rules: MutableList<CoachingBudgetRule>) {
    fun validate(item: CoachingBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingBudgetValidator(): CoachingBudgetValidator {
    return CoachingBudgetValidator(
        mutableListOf<CoachingBudgetRule>(
            CoachingBudgetRequiredLabelRule("coaching budget needs an entry"),
            CoachingBudgetRequiredCodeRule("coaching budget needs a value"),
            CoachingBudgetCodeLengthRule(64),
            CoachingBudgetAllowedLocaleRule(mutableListOf<String>("pending")),
            CoachingBudgetExpiresAtRangeRule(3600, 86400),
        ),
    )
}
