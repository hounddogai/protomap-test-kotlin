package com.acme.transport.emissions.idling.forecast

/** A rule a idling forecast must pass. Check returns its problems, or none. */
interface IdlingForecastRule {
    fun check(item: IdlingForecast): MutableList<String>
}

data class IdlingForecastRequiredDepotRule(var message: String) : IdlingForecastRule {
    override fun check(item: IdlingForecast): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingForecastCapacityRangeRule(var min: Int, var max: Int) : IdlingForecastRule {
    override fun check(item: IdlingForecast): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class IdlingForecastRequiredNameRule(var message: String) : IdlingForecastRule {
    override fun check(item: IdlingForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingForecastActiveGuardRule(var limit: Double) : IdlingForecastRule {
    override fun check(item: IdlingForecast): MutableList<String> {
        if (item.active && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class IdlingForecastRequiredCodeRule(var message: String) : IdlingForecastRule {
    override fun check(item: IdlingForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingForecastRouteCodeLengthRule(var maxLength: Int) : IdlingForecastRule {
    override fun check(item: IdlingForecast): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling forecast. */
data class IdlingForecastValidator(var rules: MutableList<IdlingForecastRule>) {
    fun validate(item: IdlingForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingForecastValidator(): IdlingForecastValidator {
    return IdlingForecastValidator(
        mutableListOf<IdlingForecastRule>(
            IdlingForecastRequiredDepotRule("idling forecast needs a value"),
            IdlingForecastCapacityRangeRule(16, 3600),
            IdlingForecastRequiredNameRule("idling forecast needs this field"),
            IdlingForecastActiveGuardRule(0.6),
            IdlingForecastRequiredCodeRule("idling forecast needs an entry"),
            IdlingForecastRouteCodeLengthRule(32),
        ),
    )
}
