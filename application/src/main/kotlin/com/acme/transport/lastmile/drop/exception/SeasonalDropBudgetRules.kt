package com.acme.transport.lastmile.drop.exception

/** A rule a seasonal drop budget must pass. Check returns its problems, or none. */
interface SeasonalDropBudgetRule {
    fun check(item: SeasonalDropBudget): MutableList<String>
}

data class SeasonalDropBudgetCodePrefixRule(var prefix: String) : SeasonalDropBudgetRule {
    override fun check(item: SeasonalDropBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropBudgetRequiredCodeRule(var message: String) : SeasonalDropBudgetRule {
    override fun check(item: SeasonalDropBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropBudgetRequiredCurrencyRule(var message: String) : SeasonalDropBudgetRule {
    override fun check(item: SeasonalDropBudget): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal drop budget. */
data class SeasonalDropBudgetValidator(var rules: MutableList<SeasonalDropBudgetRule>) {
    fun validate(item: SeasonalDropBudget): MutableList<String> {
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

    fun requireValid(item: SeasonalDropBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDropBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDropBudgetValidator(): SeasonalDropBudgetValidator {
    return SeasonalDropBudgetValidator(
        mutableListOf<SeasonalDropBudgetRule>(
            SeasonalDropBudgetCodePrefixRule("X"),
            SeasonalDropBudgetRequiredCodeRule("seasonal drop budget needs a value"),
            SeasonalDropBudgetRequiredCurrencyRule("seasonal drop budget needs a value"),
        ),
    )
}
