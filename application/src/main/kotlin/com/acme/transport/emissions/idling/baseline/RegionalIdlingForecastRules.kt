package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.text.normalize

/** A rule a regional idling forecast must pass. Check returns its problems, or none. */
interface RegionalIdlingForecastRule {
    fun check(item: RegionalIdlingForecast): MutableList<String>
}

data class RegionalIdlingForecastRequiredShiftCodeRule(var message: String) : RegionalIdlingForecastRule {
    override fun check(item: RegionalIdlingForecast): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingForecastAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : RegionalIdlingForecastRule {
    override fun check(item: RegionalIdlingForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingForecastScoreRangeRule(var min: Double, var max: Double) : RegionalIdlingForecastRule {
    override fun check(item: RegionalIdlingForecast): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class RegionalIdlingForecastRequiredCodeRule(var message: String) : RegionalIdlingForecastRule {
    override fun check(item: RegionalIdlingForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingForecastCodeLengthRule(var maxLength: Int) : RegionalIdlingForecastRule {
    override fun check(item: RegionalIdlingForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional idling forecast. */
data class RegionalIdlingForecastValidator(var rules: MutableList<RegionalIdlingForecastRule>) {
    fun validate(item: RegionalIdlingForecast): MutableList<String> {
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

    fun requireValid(item: RegionalIdlingForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIdlingForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIdlingForecastValidator(): RegionalIdlingForecastValidator {
    return RegionalIdlingForecastValidator(
        mutableListOf<RegionalIdlingForecastRule>(
            RegionalIdlingForecastRequiredShiftCodeRule("regional idling forecast needs an entry"),
            RegionalIdlingForecastAllowedShiftCodeRule(mutableListOf<String>("fallback", "tiered")),
            RegionalIdlingForecastScoreRangeRule(6.7, 1514.7),
            RegionalIdlingForecastRequiredCodeRule("regional idling forecast needs this field"),
            RegionalIdlingForecastCodeLengthRule(86400),
        ),
    )
}
