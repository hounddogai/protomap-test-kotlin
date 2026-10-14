package com.acme.transport.crewing.roster.preference

/** A rule a rolling roster forecast must pass. Check returns its problems, or none. */
interface RollingRosterForecastRule {
    fun check(item: RollingRosterForecast): MutableList<String>
}

data class RollingRosterForecastRequiredTitleRule(var message: String) : RollingRosterForecastRule {
    override fun check(item: RollingRosterForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRosterForecastLimitRangeRule(var min: Int, var max: Int) : RollingRosterForecastRule {
    override fun check(item: RollingRosterForecast): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingRosterForecastDepotLengthRule(var maxLength: Int) : RollingRosterForecastRule {
    override fun check(item: RollingRosterForecast): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling roster forecast. */
data class RollingRosterForecastValidator(var rules: MutableList<RollingRosterForecastRule>) {
    fun validate(item: RollingRosterForecast): MutableList<String> {
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

    fun requireValid(item: RollingRosterForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRosterForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRosterForecastValidator(): RollingRosterForecastValidator {
    return RollingRosterForecastValidator(
        mutableListOf<RollingRosterForecastRule>(
            RollingRosterForecastRequiredTitleRule("rolling roster forecast needs a value"),
            RollingRosterForecastLimitRangeRule(16, 255),
            RollingRosterForecastDepotLengthRule(86400),
        ),
    )
}
