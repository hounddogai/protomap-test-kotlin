package com.acme.transport.fleetops.utilization.rollup

/** A rule a scheduled utilization forecast must pass. Check returns its problems, or none. */
interface ScheduledUtilizationForecastRule {
    fun check(item: ScheduledUtilizationForecast): MutableList<String>
}

data class ScheduledUtilizationForecastSequenceRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledUtilizationForecastRule {
    override fun check(item: ScheduledUtilizationForecast): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledUtilizationForecastRequiredNameRule(
    var message: String,
) : ScheduledUtilizationForecastRule {
    override fun check(item: ScheduledUtilizationForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledUtilizationForecastThresholdRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledUtilizationForecastRule {
    override fun check(item: ScheduledUtilizationForecast): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled utilization forecast. */
data class ScheduledUtilizationForecastValidator(var rules: MutableList<ScheduledUtilizationForecastRule>) {
    fun validate(item: ScheduledUtilizationForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledUtilizationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledUtilizationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledUtilizationForecastValidator(): ScheduledUtilizationForecastValidator {
    return ScheduledUtilizationForecastValidator(
        mutableListOf<ScheduledUtilizationForecastRule>(
            ScheduledUtilizationForecastSequenceRangeRule(120, 255),
            ScheduledUtilizationForecastRequiredNameRule("scheduled utilization forecast needs an entry"),
            ScheduledUtilizationForecastThresholdRangeRule(0.1, 1749.1),
        ),
    )
}
