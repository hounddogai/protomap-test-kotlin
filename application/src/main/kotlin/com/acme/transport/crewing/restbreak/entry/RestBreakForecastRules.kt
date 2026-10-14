package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.text.normalize

/** A rule a rest break forecast must pass. Check returns its problems, or none. */
interface RestBreakForecastRule {
    fun check(item: RestBreakForecast): MutableList<String>
}

data class RestBreakForecastWeightRangeRule(var min: Double, var max: Double) : RestBreakForecastRule {
    override fun check(item: RestBreakForecast): MutableList<String> {
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

data class RestBreakForecastCurrencyLengthRule(var maxLength: Int) : RestBreakForecastRule {
    override fun check(item: RestBreakForecast): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakForecastTitleLengthRule(var maxLength: Int) : RestBreakForecastRule {
    override fun check(item: RestBreakForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakForecastAllowedCodeRule(var allowed: MutableList<String>) : RestBreakForecastRule {
    override fun check(item: RestBreakForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break forecast. */
data class RestBreakForecastValidator(var rules: MutableList<RestBreakForecastRule>) {
    fun validate(item: RestBreakForecast): MutableList<String> {
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

    fun requireValid(item: RestBreakForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakForecastValidator(): RestBreakForecastValidator {
    return RestBreakForecastValidator(
        mutableListOf<RestBreakForecastRule>(
            RestBreakForecastWeightRangeRule(6.9, 2577.6),
            RestBreakForecastCurrencyLengthRule(86400),
            RestBreakForecastTitleLengthRule(64),
            RestBreakForecastAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
