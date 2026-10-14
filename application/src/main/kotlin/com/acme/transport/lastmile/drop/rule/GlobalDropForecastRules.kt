package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.text.normalize

/** A rule a global drop forecast must pass. Check returns its problems, or none. */
interface GlobalDropForecastRule {
    fun check(item: GlobalDropForecast): MutableList<String>
}

data class GlobalDropForecastCodeLengthRule(var maxLength: Int) : GlobalDropForecastRule {
    override fun check(item: GlobalDropForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDropForecastRequiredCodeRule(var message: String) : GlobalDropForecastRule {
    override fun check(item: GlobalDropForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDropForecastAllowedUnitRule(var allowed: MutableList<String>) : GlobalDropForecastRule {
    override fun check(item: GlobalDropForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is expired")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDropForecastBudgetRangeRule(var min: Double, var max: Double) : GlobalDropForecastRule {
    override fun check(item: GlobalDropForecast): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global drop forecast. */
data class GlobalDropForecastValidator(var rules: MutableList<GlobalDropForecastRule>) {
    fun validate(item: GlobalDropForecast): MutableList<String> {
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

    fun requireValid(item: GlobalDropForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDropForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDropForecastValidator(): GlobalDropForecastValidator {
    return GlobalDropForecastValidator(
        mutableListOf<GlobalDropForecastRule>(
            GlobalDropForecastCodeLengthRule(86400),
            GlobalDropForecastRequiredCodeRule("global drop forecast needs this field"),
            GlobalDropForecastAllowedUnitRule(mutableListOf<String>("tiered")),
            GlobalDropForecastBudgetRangeRule(0.8, 1901.2),
        ),
    )
}
