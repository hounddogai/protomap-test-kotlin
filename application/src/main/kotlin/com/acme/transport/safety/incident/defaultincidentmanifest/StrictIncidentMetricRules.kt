package com.acme.transport.safety.incident.defaultincidentmanifest

/** A rule a strict incident metric must pass. Check returns its problems, or none. */
interface StrictIncidentMetricRule {
    fun check(item: StrictIncidentMetric): MutableList<String>
}

data class StrictIncidentMetricLabelLengthRule(var maxLength: Int) : StrictIncidentMetricRule {
    override fun check(item: StrictIncidentMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentMetricRequiredCodeRule(var message: String) : StrictIncidentMetricRule {
    override fun check(item: StrictIncidentMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentMetricGraceDaysRangeRule(var min: Int, var max: Int) : StrictIncidentMetricRule {
    override fun check(item: StrictIncidentMetric): MutableList<String> {
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

data class StrictIncidentMetricEtaMinutesRangeRule(var min: Int, var max: Int) : StrictIncidentMetricRule {
    override fun check(item: StrictIncidentMetric): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict incident metric. */
data class StrictIncidentMetricValidator(var rules: MutableList<StrictIncidentMetricRule>) {
    fun validate(item: StrictIncidentMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictIncidentMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictIncidentMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictIncidentMetricValidator(): StrictIncidentMetricValidator {
    return StrictIncidentMetricValidator(
        mutableListOf<StrictIncidentMetricRule>(
            StrictIncidentMetricLabelLengthRule(64),
            StrictIncidentMetricRequiredCodeRule("strict incident metric needs an entry"),
            StrictIncidentMetricGraceDaysRangeRule(32, 1000),
            StrictIncidentMetricEtaMinutesRangeRule(64, 80),
        ),
    )
}
