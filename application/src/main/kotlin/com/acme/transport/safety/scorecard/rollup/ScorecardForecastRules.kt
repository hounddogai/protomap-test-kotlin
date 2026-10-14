package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.text.normalize

/** A rule a scorecard forecast must pass. Check returns its problems, or none. */
interface ScorecardForecastRule {
    fun check(item: ScorecardForecast): MutableList<String>
}

data class ScorecardForecastQuantityRangeRule(var min: Int, var max: Int) : ScorecardForecastRule {
    override fun check(item: ScorecardForecast): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ScorecardForecastStartsAtRangeRule(var min: Int, var max: Int) : ScorecardForecastRule {
    override fun check(item: ScorecardForecast): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardForecastRequiredChecksumRule(var message: String) : ScorecardForecastRule {
    override fun check(item: ScorecardForecast): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardForecastCodePrefixRule(var prefix: String) : ScorecardForecastRule {
    override fun check(item: ScorecardForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardForecastAllowedCodeRule(var allowed: MutableList<String>) : ScorecardForecastRule {
    override fun check(item: ScorecardForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard forecast. */
data class ScorecardForecastValidator(var rules: MutableList<ScorecardForecastRule>) {
    fun validate(item: ScorecardForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardForecastValidator(): ScorecardForecastValidator {
    return ScorecardForecastValidator(
        mutableListOf<ScorecardForecastRule>(
            ScorecardForecastQuantityRangeRule(16, 3600),
            ScorecardForecastStartsAtRangeRule(64, 120),
            ScorecardForecastRequiredChecksumRule("scorecard forecast needs a value"),
            ScorecardForecastCodePrefixRule("Q"),
            ScorecardForecastAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
