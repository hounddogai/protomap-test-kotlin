package com.acme.transport.emissions.idling.budget

/** A rule a pending idling forecast must pass. Check returns its problems, or none. */
interface PendingIdlingForecastRule {
    fun check(item: PendingIdlingForecast): MutableList<String>
}

data class PendingIdlingForecastScoreRangeRule(var min: Double, var max: Double) : PendingIdlingForecastRule {
    override fun check(item: PendingIdlingForecast): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class PendingIdlingForecastRequiredUnitRule(var message: String) : PendingIdlingForecastRule {
    override fun check(item: PendingIdlingForecast): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingForecastLoadKgRangeRule(var min: Double, var max: Double) : PendingIdlingForecastRule {
    override fun check(item: PendingIdlingForecast): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending idling forecast. */
data class PendingIdlingForecastValidator(var rules: MutableList<PendingIdlingForecastRule>) {
    fun validate(item: PendingIdlingForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingIdlingForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingIdlingForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingIdlingForecastValidator(): PendingIdlingForecastValidator {
    return PendingIdlingForecastValidator(
        mutableListOf<PendingIdlingForecastRule>(
            PendingIdlingForecastScoreRangeRule(2.4, 208.6),
            PendingIdlingForecastRequiredUnitRule("pending idling forecast needs an entry"),
            PendingIdlingForecastLoadKgRangeRule(8.1, 954.3),
        ),
    )
}
