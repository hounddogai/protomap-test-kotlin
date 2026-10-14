package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.text.normalize

/** A rule a scheduled incident forecast must pass. Check returns its problems, or none. */
interface ScheduledIncidentForecastRule {
    fun check(item: ScheduledIncidentForecast): MutableList<String>
}

data class ScheduledIncidentForecastRequiredNameRule(var message: String) : ScheduledIncidentForecastRule {
    override fun check(item: ScheduledIncidentForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIncidentForecastAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : ScheduledIncidentForecastRule {
    override fun check(item: ScheduledIncidentForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIncidentForecastRetriesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledIncidentForecastRule {
    override fun check(item: ScheduledIncidentForecast): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIncidentForecastRequiredCodeRule(var message: String) : ScheduledIncidentForecastRule {
    override fun check(item: ScheduledIncidentForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled incident forecast. */
data class ScheduledIncidentForecastValidator(var rules: MutableList<ScheduledIncidentForecastRule>) {
    fun validate(item: ScheduledIncidentForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledIncidentForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledIncidentForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledIncidentForecastValidator(): ScheduledIncidentForecastValidator {
    return ScheduledIncidentForecastValidator(
        mutableListOf<ScheduledIncidentForecastRule>(
            ScheduledIncidentForecastRequiredNameRule("scheduled incident forecast needs an entry"),
            ScheduledIncidentForecastAllowedExternalRefRule(mutableListOf<String>("regional")),
            ScheduledIncidentForecastRetriesRangeRule(32, 86400),
            ScheduledIncidentForecastRequiredCodeRule("scheduled incident forecast needs this field"),
        ),
    )
}
