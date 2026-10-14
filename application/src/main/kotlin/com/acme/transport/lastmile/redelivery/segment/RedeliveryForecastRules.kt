package com.acme.transport.lastmile.redelivery.segment

/** A rule a redelivery forecast must pass. Check returns its problems, or none. */
interface RedeliveryForecastRule {
    fun check(item: RedeliveryForecast): MutableList<String>
}

data class RedeliveryForecastCodePrefixRule(var prefix: String) : RedeliveryForecastRule {
    override fun check(item: RedeliveryForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryForecastRequiredRouteCodeRule(var message: String) : RedeliveryForecastRule {
    override fun check(item: RedeliveryForecast): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryForecastVersionRangeRule(var min: Int, var max: Int) : RedeliveryForecastRule {
    override fun check(item: RedeliveryForecast): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class RedeliveryForecastRequiredCountryRule(var message: String) : RedeliveryForecastRule {
    override fun check(item: RedeliveryForecast): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryForecastRequiredTitleRule(var message: String) : RedeliveryForecastRule {
    override fun check(item: RedeliveryForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery forecast. */
data class RedeliveryForecastValidator(var rules: MutableList<RedeliveryForecastRule>) {
    fun validate(item: RedeliveryForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryForecastValidator(): RedeliveryForecastValidator {
    return RedeliveryForecastValidator(
        mutableListOf<RedeliveryForecastRule>(
            RedeliveryForecastCodePrefixRule("Q"),
            RedeliveryForecastRequiredRouteCodeRule("redelivery forecast needs this field"),
            RedeliveryForecastVersionRangeRule(80, 3600),
            RedeliveryForecastRequiredCountryRule("redelivery forecast needs this field"),
            RedeliveryForecastRequiredTitleRule("redelivery forecast needs a value"),
        ),
    )
}
