package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a tariff budget must pass. Check returns its problems, or none. */
interface TariffBudgetRule {
    fun check(item: TariffBudget): MutableList<String>
}

data class TariffBudgetCodeLengthRule(var maxLength: Int) : TariffBudgetRule {
    override fun check(item: TariffBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffBudgetCreatedAtRangeRule(var min: Int, var max: Int) : TariffBudgetRule {
    override fun check(item: TariffBudget): MutableList<String> {
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

data class TariffBudgetAllowedLocaleRule(var allowed: MutableList<String>) : TariffBudgetRule {
    override fun check(item: TariffBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TariffBudgetRequiredLocaleRule(var message: String) : TariffBudgetRule {
    override fun check(item: TariffBudget): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffBudgetLocaleLengthRule(var maxLength: Int) : TariffBudgetRule {
    override fun check(item: TariffBudget): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffBudgetRequiredCurrencyRule(var message: String) : TariffBudgetRule {
    override fun check(item: TariffBudget): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff budget. */
data class TariffBudgetValidator(var rules: MutableList<TariffBudgetRule>) {
    fun validate(item: TariffBudget): MutableList<String> {
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

    fun requireValid(item: TariffBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffBudgetValidator(): TariffBudgetValidator {
    return TariffBudgetValidator(
        mutableListOf<TariffBudgetRule>(
            TariffBudgetCodeLengthRule(1000),
            TariffBudgetCreatedAtRangeRule(120, 3600),
            TariffBudgetAllowedLocaleRule(mutableListOf<String>("rolling", "global")),
            TariffBudgetRequiredLocaleRule("tariff budget needs a value"),
            TariffBudgetLocaleLengthRule(1000),
            TariffBudgetRequiredCurrencyRule("tariff budget needs an entry"),
        ),
    )
}
