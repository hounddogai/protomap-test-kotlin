package com.acme.transport.safety.scorecard.rule

/** A rule a pending scorecard forecast must pass. Check returns its problems, or none. */
interface PendingScorecardForecastRule {
    fun check(item: PendingScorecardForecast): MutableList<String>
}

data class PendingScorecardForecastCodePrefixRule(var prefix: String) : PendingScorecardForecastRule {
    override fun check(item: PendingScorecardForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardForecastExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : PendingScorecardForecastRule {
    override fun check(item: PendingScorecardForecast): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class PendingScorecardForecastWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : PendingScorecardForecastRule {
    override fun check(item: PendingScorecardForecast): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardForecastRequiredNameRule(var message: String) : PendingScorecardForecastRule {
    override fun check(item: PendingScorecardForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending scorecard forecast. */
data class PendingScorecardForecastValidator(var rules: MutableList<PendingScorecardForecastRule>) {
    fun validate(item: PendingScorecardForecast): MutableList<String> {
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

    fun requireValid(item: PendingScorecardForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingScorecardForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingScorecardForecastValidator(): PendingScorecardForecastValidator {
    return PendingScorecardForecastValidator(
        mutableListOf<PendingScorecardForecastRule>(
            PendingScorecardForecastCodePrefixRule("OPS"),
            PendingScorecardForecastExpiresAtRangeRule(255, 86400),
            PendingScorecardForecastWindowDaysRangeRule(120, 255),
            PendingScorecardForecastRequiredNameRule("pending scorecard forecast needs this field"),
        ),
    )
}
