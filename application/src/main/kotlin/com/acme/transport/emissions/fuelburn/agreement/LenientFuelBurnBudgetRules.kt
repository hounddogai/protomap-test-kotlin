package com.acme.transport.emissions.fuelburn.agreement

/** A rule a lenient fuel burn budget must pass. Check returns its problems, or none. */
interface LenientFuelBurnBudgetRule {
    fun check(item: LenientFuelBurnBudget): MutableList<String>
}

data class LenientFuelBurnBudgetRequiredCodeRule(var message: String) : LenientFuelBurnBudgetRule {
    override fun check(item: LenientFuelBurnBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnBudgetExpiresAtRangeRule(var min: Int, var max: Int) : LenientFuelBurnBudgetRule {
    override fun check(item: LenientFuelBurnBudget): MutableList<String> {
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

data class LenientFuelBurnBudgetLimitRangeRule(var min: Int, var max: Int) : LenientFuelBurnBudgetRule {
    override fun check(item: LenientFuelBurnBudget): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnBudgetWeightRangeRule(var min: Double, var max: Double) : LenientFuelBurnBudgetRule {
    override fun check(item: LenientFuelBurnBudget): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient fuel burn budget. */
data class LenientFuelBurnBudgetValidator(var rules: MutableList<LenientFuelBurnBudgetRule>) {
    fun validate(item: LenientFuelBurnBudget): MutableList<String> {
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

    fun requireValid(item: LenientFuelBurnBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFuelBurnBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFuelBurnBudgetValidator(): LenientFuelBurnBudgetValidator {
    return LenientFuelBurnBudgetValidator(
        mutableListOf<LenientFuelBurnBudgetRule>(
            LenientFuelBurnBudgetRequiredCodeRule("lenient fuel burn budget needs a value"),
            LenientFuelBurnBudgetExpiresAtRangeRule(16, 1000),
            LenientFuelBurnBudgetLimitRangeRule(32, 3600),
            LenientFuelBurnBudgetWeightRangeRule(2.7, 3454.6),
        ),
    )
}
