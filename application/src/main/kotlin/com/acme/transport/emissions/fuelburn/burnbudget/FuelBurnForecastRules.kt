package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn forecast must pass. Check returns its problems, or none. */
interface FuelBurnForecastRule {
    fun check(item: FuelBurnForecast): MutableList<String>
}

data class FuelBurnForecastRequiredCurrencyRule(var message: String) : FuelBurnForecastRule {
    override fun check(item: FuelBurnForecast): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnForecastStartsAtRangeRule(var min: Int, var max: Int) : FuelBurnForecastRule {
    override fun check(item: FuelBurnForecast): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class FuelBurnForecastArchivedGuardRule(var limit: Double) : FuelBurnForecastRule {
    override fun check(item: FuelBurnForecast): MutableList<String> {
        if (item.archived && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnForecastAllowedCurrencyRule(var allowed: MutableList<String>) : FuelBurnForecastRule {
    override fun check(item: FuelBurnForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnForecastCodeLengthRule(var maxLength: Int) : FuelBurnForecastRule {
    override fun check(item: FuelBurnForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn forecast. */
data class FuelBurnForecastValidator(var rules: MutableList<FuelBurnForecastRule>) {
    fun validate(item: FuelBurnForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnForecastValidator(): FuelBurnForecastValidator {
    return FuelBurnForecastValidator(
        mutableListOf<FuelBurnForecastRule>(
            FuelBurnForecastRequiredCurrencyRule("fuel burn forecast needs this field"),
            FuelBurnForecastStartsAtRangeRule(16, 3600),
            FuelBurnForecastArchivedGuardRule(7.6),
            FuelBurnForecastAllowedCurrencyRule(mutableListOf<String>("manual", "legacy")),
            FuelBurnForecastCodeLengthRule(255),
        ),
    )
}
