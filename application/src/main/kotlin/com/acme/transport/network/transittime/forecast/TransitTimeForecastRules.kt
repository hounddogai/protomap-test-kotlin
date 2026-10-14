package com.acme.transport.network.transittime.forecast

/** A rule a transit time forecast must pass. Check returns its problems, or none. */
interface TransitTimeForecastRule {
    fun check(item: TransitTimeForecast): MutableList<String>
}

data class TransitTimeForecastCodeLengthRule(var maxLength: Int) : TransitTimeForecastRule {
    override fun check(item: TransitTimeForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeForecastRequiredRouteCodeRule(var message: String) : TransitTimeForecastRule {
    override fun check(item: TransitTimeForecast): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeForecastExternalRefLengthRule(var maxLength: Int) : TransitTimeForecastRule {
    override fun check(item: TransitTimeForecast): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time forecast. */
data class TransitTimeForecastValidator(var rules: MutableList<TransitTimeForecastRule>) {
    fun validate(item: TransitTimeForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeForecastValidator(): TransitTimeForecastValidator {
    return TransitTimeForecastValidator(
        mutableListOf<TransitTimeForecastRule>(
            TransitTimeForecastCodeLengthRule(120),
            TransitTimeForecastRequiredRouteCodeRule("transit time forecast needs this field"),
            TransitTimeForecastExternalRefLengthRule(255),
        ),
    )
}
