package com.acme.transport.safety.incident.incidentsnapshot

/** A rule a incident forecast must pass. Check returns its problems, or none. */
interface IncidentForecastRule {
    fun check(item: IncidentForecast): MutableList<String>
}

data class IncidentForecastStartsAtRangeRule(var min: Int, var max: Int) : IncidentForecastRule {
    override fun check(item: IncidentForecast): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class IncidentForecastArchivedGuardRule(var limit: Double) : IncidentForecastRule {
    override fun check(item: IncidentForecast): MutableList<String> {
        if (item.archived && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class IncidentForecastGraceDaysRangeRule(var min: Int, var max: Int) : IncidentForecastRule {
    override fun check(item: IncidentForecast): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a incident forecast. */
data class IncidentForecastValidator(var rules: MutableList<IncidentForecastRule>) {
    fun validate(item: IncidentForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentForecastValidator(): IncidentForecastValidator {
    return IncidentForecastValidator(
        mutableListOf<IncidentForecastRule>(
            IncidentForecastStartsAtRangeRule(86400, 86400),
            IncidentForecastArchivedGuardRule(8.0),
            IncidentForecastGraceDaysRangeRule(80, 86400),
        ),
    )
}
