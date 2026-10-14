package com.acme.transport.emissions.routeoffset.forecast

/** A rule a route offset forecast must pass. Check returns its problems, or none. */
interface RouteOffsetForecastRule {
    fun check(item: RouteOffsetForecast): MutableList<String>
}

data class RouteOffsetForecastRequiredNameRule(var message: String) : RouteOffsetForecastRule {
    override fun check(item: RouteOffsetForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetForecastOwnerIdLengthRule(var maxLength: Int) : RouteOffsetForecastRule {
    override fun check(item: RouteOffsetForecast): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetForecastCodePrefixRule(var prefix: String) : RouteOffsetForecastRule {
    override fun check(item: RouteOffsetForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetForecastRequiredOwnerIdRule(var message: String) : RouteOffsetForecastRule {
    override fun check(item: RouteOffsetForecast): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetForecastCodeLengthRule(var maxLength: Int) : RouteOffsetForecastRule {
    override fun check(item: RouteOffsetForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset forecast. */
data class RouteOffsetForecastValidator(var rules: MutableList<RouteOffsetForecastRule>) {
    fun validate(item: RouteOffsetForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetForecastValidator(): RouteOffsetForecastValidator {
    return RouteOffsetForecastValidator(
        mutableListOf<RouteOffsetForecastRule>(
            RouteOffsetForecastRequiredNameRule("route offset forecast needs an entry"),
            RouteOffsetForecastOwnerIdLengthRule(80),
            RouteOffsetForecastCodePrefixRule("ACM"),
            RouteOffsetForecastRequiredOwnerIdRule("route offset forecast needs an entry"),
            RouteOffsetForecastCodeLengthRule(86400),
        ),
    )
}
