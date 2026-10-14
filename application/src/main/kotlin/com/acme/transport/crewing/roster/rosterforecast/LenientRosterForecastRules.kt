package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.text.normalize

/** A rule a lenient roster forecast must pass. Check returns its problems, or none. */
interface LenientRosterForecastRule {
    fun check(item: LenientRosterForecast): MutableList<String>
}

data class LenientRosterForecastMaximumRangeRule(var min: Double, var max: Double) : LenientRosterForecastRule {
    override fun check(item: LenientRosterForecast): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class LenientRosterForecastAllowedLocaleRule(
    var allowed: MutableList<String>,
) : LenientRosterForecastRule {
    override fun check(item: LenientRosterForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterForecastRequiredNameRule(var message: String) : LenientRosterForecastRule {
    override fun check(item: LenientRosterForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient roster forecast. */
data class LenientRosterForecastValidator(var rules: MutableList<LenientRosterForecastRule>) {
    fun validate(item: LenientRosterForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRosterForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRosterForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRosterForecastValidator(): LenientRosterForecastValidator {
    return LenientRosterForecastValidator(
        mutableListOf<LenientRosterForecastRule>(
            LenientRosterForecastMaximumRangeRule(8.3, 3114.0),
            LenientRosterForecastAllowedLocaleRule(mutableListOf<String>()),
            LenientRosterForecastRequiredNameRule("lenient roster forecast needs an entry"),
        ),
    )
}
