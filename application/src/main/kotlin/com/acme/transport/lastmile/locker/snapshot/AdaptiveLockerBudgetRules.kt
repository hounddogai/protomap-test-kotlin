package com.acme.transport.lastmile.locker.snapshot

/** A rule a adaptive locker budget must pass. Check returns its problems, or none. */
interface AdaptiveLockerBudgetRule {
    fun check(item: AdaptiveLockerBudget): MutableList<String>
}

data class AdaptiveLockerBudgetAmountRangeRule(var min: Double, var max: Double) : AdaptiveLockerBudgetRule {
    override fun check(item: AdaptiveLockerBudget): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class AdaptiveLockerBudgetRequiredCodeRule(var message: String) : AdaptiveLockerBudgetRule {
    override fun check(item: AdaptiveLockerBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLockerBudgetRequiredSourceRule(var message: String) : AdaptiveLockerBudgetRule {
    override fun check(item: AdaptiveLockerBudget): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLockerBudgetRequiredLabelRule(var message: String) : AdaptiveLockerBudgetRule {
    override fun check(item: AdaptiveLockerBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive locker budget. */
data class AdaptiveLockerBudgetValidator(var rules: MutableList<AdaptiveLockerBudgetRule>) {
    fun validate(item: AdaptiveLockerBudget): MutableList<String> {
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

    fun requireValid(item: AdaptiveLockerBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLockerBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLockerBudgetValidator(): AdaptiveLockerBudgetValidator {
    return AdaptiveLockerBudgetValidator(
        mutableListOf<AdaptiveLockerBudgetRule>(
            AdaptiveLockerBudgetAmountRangeRule(8.4, 1761.3),
            AdaptiveLockerBudgetRequiredCodeRule("adaptive locker budget needs this field"),
            AdaptiveLockerBudgetRequiredSourceRule("adaptive locker budget needs this field"),
            AdaptiveLockerBudgetRequiredLabelRule("adaptive locker budget needs an entry"),
        ),
    )
}
