package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.text.normalize

/** A rule a legacy idling forecast must pass. Check returns its problems, or none. */
interface LegacyIdlingForecastRule {
    fun check(item: LegacyIdlingForecast): MutableList<String>
}

data class LegacyIdlingForecastLimitRangeRule(var min: Int, var max: Int) : LegacyIdlingForecastRule {
    override fun check(item: LegacyIdlingForecast): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIdlingForecastRequiredSourceRule(var message: String) : LegacyIdlingForecastRule {
    override fun check(item: LegacyIdlingForecast): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIdlingForecastCodePrefixRule(var prefix: String) : LegacyIdlingForecastRule {
    override fun check(item: LegacyIdlingForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIdlingForecastEndsAtRangeRule(var min: Int, var max: Int) : LegacyIdlingForecastRule {
    override fun check(item: LegacyIdlingForecast): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class LegacyIdlingForecastAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : LegacyIdlingForecastRule {
    override fun check(item: LegacyIdlingForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy idling forecast. */
data class LegacyIdlingForecastValidator(var rules: MutableList<LegacyIdlingForecastRule>) {
    fun validate(item: LegacyIdlingForecast): MutableList<String> {
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

    fun requireValid(item: LegacyIdlingForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyIdlingForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyIdlingForecastValidator(): LegacyIdlingForecastValidator {
    return LegacyIdlingForecastValidator(
        mutableListOf<LegacyIdlingForecastRule>(
            LegacyIdlingForecastLimitRangeRule(64, 86400),
            LegacyIdlingForecastRequiredSourceRule("legacy idling forecast needs a value"),
            LegacyIdlingForecastCodePrefixRule("REF"),
            LegacyIdlingForecastEndsAtRangeRule(16, 32),
            LegacyIdlingForecastAllowedVehicleIdRule(mutableListOf<String>("scheduled")),
        ),
    )
}
