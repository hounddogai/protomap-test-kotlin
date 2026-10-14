package com.acme.transport.terminal.traileryard.yardforecast

/** A rule a regional trailer yard forecast must pass. Check returns its problems, or none. */
interface RegionalTrailerYardForecastRule {
    fun check(item: RegionalTrailerYardForecast): MutableList<String>
}

data class RegionalTrailerYardForecastDescriptionLengthRule(
    var maxLength: Int,
) : RegionalTrailerYardForecastRule {
    override fun check(item: RegionalTrailerYardForecast): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardForecastTitleLengthRule(var maxLength: Int) : RegionalTrailerYardForecastRule {
    override fun check(item: RegionalTrailerYardForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardForecastThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RegionalTrailerYardForecastRule {
    override fun check(item: RegionalTrailerYardForecast): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class RegionalTrailerYardForecastPinnedGuardRule(var limit: Double) : RegionalTrailerYardForecastRule {
    override fun check(item: RegionalTrailerYardForecast): MutableList<String> {
        if (item.pinned && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional trailer yard forecast. */
data class RegionalTrailerYardForecastValidator(var rules: MutableList<RegionalTrailerYardForecastRule>) {
    fun validate(item: RegionalTrailerYardForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalTrailerYardForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerYardForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerYardForecastValidator(): RegionalTrailerYardForecastValidator {
    return RegionalTrailerYardForecastValidator(
        mutableListOf<RegionalTrailerYardForecastRule>(
            RegionalTrailerYardForecastDescriptionLengthRule(64),
            RegionalTrailerYardForecastTitleLengthRule(1000),
            RegionalTrailerYardForecastThresholdRangeRule(1.1, 83.2),
            RegionalTrailerYardForecastPinnedGuardRule(4.6),
        ),
    )
}
