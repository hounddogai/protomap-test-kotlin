package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.text.normalize

/** A rule a weighted appointment score must pass. Check returns its problems, or none. */
interface WeightedAppointmentScoreRule {
    fun check(item: WeightedAppointmentScore): MutableList<String>
}

data class WeightedAppointmentScoreStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedAppointmentScoreRule {
    override fun check(item: WeightedAppointmentScore): MutableList<String> {
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

data class WeightedAppointmentScoreEndsAtRangeRule(var min: Int, var max: Int) : WeightedAppointmentScoreRule {
    override fun check(item: WeightedAppointmentScore): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAppointmentScoreAllowedSourceRule(
    var allowed: MutableList<String>,
) : WeightedAppointmentScoreRule {
    override fun check(item: WeightedAppointmentScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted appointment score. */
data class WeightedAppointmentScoreValidator(var rules: MutableList<WeightedAppointmentScoreRule>) {
    fun validate(item: WeightedAppointmentScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAppointmentScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAppointmentScore>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAppointmentScoreValidator(): WeightedAppointmentScoreValidator {
    return WeightedAppointmentScoreValidator(
        mutableListOf<WeightedAppointmentScoreRule>(
            WeightedAppointmentScoreStartsAtRangeRule(86400, 86400),
            WeightedAppointmentScoreEndsAtRangeRule(255, 1000),
            WeightedAppointmentScoreAllowedSourceRule(mutableListOf<String>("lenient", "primary")),
        ),
    )
}
