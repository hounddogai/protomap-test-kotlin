package com.acme.transport.customs.hold.forecast

/** A rule a scheduled hold forecast must pass. Check returns its problems, or none. */
interface ScheduledHoldForecastRule {
    fun check(item: ScheduledHoldForecast): MutableList<String>
}

data class ScheduledHoldForecastStartsAtRangeRule(var min: Int, var max: Int) : ScheduledHoldForecastRule {
    override fun check(item: ScheduledHoldForecast): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldForecastRequiredCodeRule(var message: String) : ScheduledHoldForecastRule {
    override fun check(item: ScheduledHoldForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldForecastRequiredCountryRule(var message: String) : ScheduledHoldForecastRule {
    override fun check(item: ScheduledHoldForecast): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hold forecast. */
data class ScheduledHoldForecastValidator(var rules: MutableList<ScheduledHoldForecastRule>) {
    fun validate(item: ScheduledHoldForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledHoldForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHoldForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHoldForecastValidator(): ScheduledHoldForecastValidator {
    return ScheduledHoldForecastValidator(
        mutableListOf<ScheduledHoldForecastRule>(
            ScheduledHoldForecastStartsAtRangeRule(80, 86400),
            ScheduledHoldForecastRequiredCodeRule("scheduled hold forecast needs this field"),
            ScheduledHoldForecastRequiredCountryRule("scheduled hold forecast needs this field"),
        ),
    )
}
