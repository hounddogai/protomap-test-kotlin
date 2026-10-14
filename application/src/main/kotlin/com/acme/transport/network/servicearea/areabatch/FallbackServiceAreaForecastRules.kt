package com.acme.transport.network.servicearea.areabatch

/** A rule a fallback service area forecast must pass. Check returns its problems, or none. */
interface FallbackServiceAreaForecastRule {
    fun check(item: FallbackServiceAreaForecast): MutableList<String>
}

data class FallbackServiceAreaForecastRequiredCodeRule(var message: String) : FallbackServiceAreaForecastRule {
    override fun check(item: FallbackServiceAreaForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackServiceAreaForecastUnitLengthRule(var maxLength: Int) : FallbackServiceAreaForecastRule {
    override fun check(item: FallbackServiceAreaForecast): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServiceAreaForecastCodePrefixRule(var prefix: String) : FallbackServiceAreaForecastRule {
    override fun check(item: FallbackServiceAreaForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback service area forecast. */
data class FallbackServiceAreaForecastValidator(var rules: MutableList<FallbackServiceAreaForecastRule>) {
    fun validate(item: FallbackServiceAreaForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackServiceAreaForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackServiceAreaForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackServiceAreaForecastValidator(): FallbackServiceAreaForecastValidator {
    return FallbackServiceAreaForecastValidator(
        mutableListOf<FallbackServiceAreaForecastRule>(
            FallbackServiceAreaForecastRequiredCodeRule("fallback service area forecast needs this field"),
            FallbackServiceAreaForecastUnitLengthRule(64),
            FallbackServiceAreaForecastCodePrefixRule("X"),
        ),
    )
}
