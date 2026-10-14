package com.acme.transport.network.hub.milestone

/** A rule a tiered hub budget must pass. Check returns its problems, or none. */
interface TieredHubBudgetRule {
    fun check(item: TieredHubBudget): MutableList<String>
}

data class TieredHubBudgetLabelLengthRule(var maxLength: Int) : TieredHubBudgetRule {
    override fun check(item: TieredHubBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredHubBudgetRetriesRangeRule(var min: Int, var max: Int) : TieredHubBudgetRule {
    override fun check(item: TieredHubBudget): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredHubBudgetRequiredCodeRule(var message: String) : TieredHubBudgetRule {
    override fun check(item: TieredHubBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered hub budget. */
data class TieredHubBudgetValidator(var rules: MutableList<TieredHubBudgetRule>) {
    fun validate(item: TieredHubBudget): MutableList<String> {
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

    fun requireValid(item: TieredHubBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredHubBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredHubBudgetValidator(): TieredHubBudgetValidator {
    return TieredHubBudgetValidator(
        mutableListOf<TieredHubBudgetRule>(
            TieredHubBudgetLabelLengthRule(80),
            TieredHubBudgetRetriesRangeRule(32, 1000),
            TieredHubBudgetRequiredCodeRule("tiered hub budget needs an entry"),
        ),
    )
}
