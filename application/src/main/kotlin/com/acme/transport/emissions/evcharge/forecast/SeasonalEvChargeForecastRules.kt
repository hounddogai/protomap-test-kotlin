package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.text.normalize

/** A rule a seasonal ev charge forecast must pass. Check returns its problems, or none. */
interface SeasonalEvChargeForecastRule {
    fun check(item: SeasonalEvChargeForecast): MutableList<String>
}

data class SeasonalEvChargeForecastAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : SeasonalEvChargeForecastRule {
    override fun check(item: SeasonalEvChargeForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEvChargeForecastNameLengthRule(var maxLength: Int) : SeasonalEvChargeForecastRule {
    override fun check(item: SeasonalEvChargeForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEvChargeForecastCodeLengthRule(var maxLength: Int) : SeasonalEvChargeForecastRule {
    override fun check(item: SeasonalEvChargeForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal ev charge forecast. */
data class SeasonalEvChargeForecastValidator(var rules: MutableList<SeasonalEvChargeForecastRule>) {
    fun validate(item: SeasonalEvChargeForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalEvChargeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEvChargeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEvChargeForecastValidator(): SeasonalEvChargeForecastValidator {
    return SeasonalEvChargeForecastValidator(
        mutableListOf<SeasonalEvChargeForecastRule>(
            SeasonalEvChargeForecastAllowedExternalRefRule(mutableListOf<String>()),
            SeasonalEvChargeForecastNameLengthRule(80),
            SeasonalEvChargeForecastCodeLengthRule(1000),
        ),
    )
}
