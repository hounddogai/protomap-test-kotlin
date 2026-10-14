package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.text.normalize

/** A rule a seasonal roster forecast must pass. Check returns its problems, or none. */
interface SeasonalRosterForecastRule {
    fun check(item: SeasonalRosterForecast): MutableList<String>
}

data class SeasonalRosterForecastExpiresAtRangeRule(var min: Int, var max: Int) : SeasonalRosterForecastRule {
    override fun check(item: SeasonalRosterForecast): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRosterForecastAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalRosterForecastRule {
    override fun check(item: SeasonalRosterForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRosterForecastRequiredSummaryRule(var message: String) : SeasonalRosterForecastRule {
    override fun check(item: SeasonalRosterForecast): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRosterForecastWeightRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRosterForecastRule {
    override fun check(item: SeasonalRosterForecast): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class SeasonalRosterForecastRequiredLabelRule(var message: String) : SeasonalRosterForecastRule {
    override fun check(item: SeasonalRosterForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRosterForecastLabelLengthRule(var maxLength: Int) : SeasonalRosterForecastRule {
    override fun check(item: SeasonalRosterForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal roster forecast. */
data class SeasonalRosterForecastValidator(var rules: MutableList<SeasonalRosterForecastRule>) {
    fun validate(item: SeasonalRosterForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRosterForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRosterForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRosterForecastValidator(): SeasonalRosterForecastValidator {
    return SeasonalRosterForecastValidator(
        mutableListOf<SeasonalRosterForecastRule>(
            SeasonalRosterForecastExpiresAtRangeRule(1000, 86400),
            SeasonalRosterForecastAllowedCodeRule(mutableListOf<String>()),
            SeasonalRosterForecastRequiredSummaryRule("seasonal roster forecast needs an entry"),
            SeasonalRosterForecastWeightRangeRule(5.0, 4968.1),
            SeasonalRosterForecastRequiredLabelRule("seasonal roster forecast needs this field"),
            SeasonalRosterForecastLabelLengthRule(16),
        ),
    )
}
