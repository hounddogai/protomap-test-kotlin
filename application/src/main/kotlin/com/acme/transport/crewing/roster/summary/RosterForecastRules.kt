package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.text.normalize

/** A rule a roster forecast must pass. Check returns its problems, or none. */
interface RosterForecastRule {
    fun check(item: RosterForecast): MutableList<String>
}

data class RosterForecastAllowedCodeRule(var allowed: MutableList<String>) : RosterForecastRule {
    override fun check(item: RosterForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RosterForecastCodePrefixRule(var prefix: String) : RosterForecastRule {
    override fun check(item: RosterForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterForecastRequiredLaneRule(var message: String) : RosterForecastRule {
    override fun check(item: RosterForecast): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterForecastPriorityRangeRule(var min: Int, var max: Int) : RosterForecastRule {
    override fun check(item: RosterForecast): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class RosterForecastLaneLengthRule(var maxLength: Int) : RosterForecastRule {
    override fun check(item: RosterForecast): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster forecast. */
data class RosterForecastValidator(var rules: MutableList<RosterForecastRule>) {
    fun validate(item: RosterForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterForecastValidator(): RosterForecastValidator {
    return RosterForecastValidator(
        mutableListOf<RosterForecastRule>(
            RosterForecastAllowedCodeRule(mutableListOf<String>()),
            RosterForecastCodePrefixRule("ACM"),
            RosterForecastRequiredLaneRule("roster forecast needs this field"),
            RosterForecastPriorityRangeRule(16, 1000),
            RosterForecastLaneLengthRule(86400),
        ),
    )
}
