package com.acme.transport.customs.tariff.budget

/** A rule a regional tariff budget must pass. Check returns its problems, or none. */
interface RegionalTariffBudgetRule {
    fun check(item: RegionalTariffBudget): MutableList<String>
}

data class RegionalTariffBudgetCreatedAtRangeRule(var min: Int, var max: Int) : RegionalTariffBudgetRule {
    override fun check(item: RegionalTariffBudget): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class RegionalTariffBudgetAmountRangeRule(var min: Double, var max: Double) : RegionalTariffBudgetRule {
    override fun check(item: RegionalTariffBudget): MutableList<String> {
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

data class RegionalTariffBudgetCodeLengthRule(var maxLength: Int) : RegionalTariffBudgetRule {
    override fun check(item: RegionalTariffBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTariffBudgetCodePrefixRule(var prefix: String) : RegionalTariffBudgetRule {
    override fun check(item: RegionalTariffBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTariffBudgetLabelLengthRule(var maxLength: Int) : RegionalTariffBudgetRule {
    override fun check(item: RegionalTariffBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTariffBudgetMarginRangeRule(var min: Double, var max: Double) : RegionalTariffBudgetRule {
    override fun check(item: RegionalTariffBudget): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional tariff budget. */
data class RegionalTariffBudgetValidator(var rules: MutableList<RegionalTariffBudgetRule>) {
    fun validate(item: RegionalTariffBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalTariffBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTariffBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTariffBudgetValidator(): RegionalTariffBudgetValidator {
    return RegionalTariffBudgetValidator(
        mutableListOf<RegionalTariffBudgetRule>(
            RegionalTariffBudgetCreatedAtRangeRule(16, 255),
            RegionalTariffBudgetAmountRangeRule(6.3, 4365.4),
            RegionalTariffBudgetCodeLengthRule(120),
            RegionalTariffBudgetCodePrefixRule("X"),
            RegionalTariffBudgetLabelLengthRule(3600),
            RegionalTariffBudgetMarginRangeRule(5.2, 3077.9),
        ),
    )
}
