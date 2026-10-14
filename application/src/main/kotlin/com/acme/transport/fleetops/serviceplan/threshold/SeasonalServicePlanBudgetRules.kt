package com.acme.transport.fleetops.serviceplan.threshold

/** A rule a seasonal service plan budget must pass. Check returns its problems, or none. */
interface SeasonalServicePlanBudgetRule {
    fun check(item: SeasonalServicePlanBudget): MutableList<String>
}

data class SeasonalServicePlanBudgetCodePrefixRule(var prefix: String) : SeasonalServicePlanBudgetRule {
    override fun check(item: SeasonalServicePlanBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanBudgetRevisionRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalServicePlanBudgetRule {
    override fun check(item: SeasonalServicePlanBudget): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanBudgetRequiredCodeRule(var message: String) : SeasonalServicePlanBudgetRule {
    override fun check(item: SeasonalServicePlanBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanBudgetQuantityRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalServicePlanBudgetRule {
    override fun check(item: SeasonalServicePlanBudget): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal service plan budget. */
data class SeasonalServicePlanBudgetValidator(var rules: MutableList<SeasonalServicePlanBudgetRule>) {
    fun validate(item: SeasonalServicePlanBudget): MutableList<String> {
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

    fun requireValid(item: SeasonalServicePlanBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalServicePlanBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalServicePlanBudgetValidator(): SeasonalServicePlanBudgetValidator {
    return SeasonalServicePlanBudgetValidator(
        mutableListOf<SeasonalServicePlanBudgetRule>(
            SeasonalServicePlanBudgetCodePrefixRule("REF"),
            SeasonalServicePlanBudgetRevisionRangeRule(120, 120),
            SeasonalServicePlanBudgetRequiredCodeRule("seasonal service plan budget needs a value"),
            SeasonalServicePlanBudgetQuantityRangeRule(80, 120),
        ),
    )
}
