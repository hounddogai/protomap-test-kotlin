package com.acme.transport.terminal.appointment.appointmentsegment

import com.acme.transport.kit.text.normalize

/** A rule a adaptive appointment forecast must pass. Check returns its problems, or none. */
interface AdaptiveAppointmentForecastRule {
    fun check(item: AdaptiveAppointmentForecast): MutableList<String>
}

data class AdaptiveAppointmentForecastDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveAppointmentForecastRule {
    override fun check(item: AdaptiveAppointmentForecast): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentForecastAllowedCarrierRule(
    var allowed: MutableList<String>,
) : AdaptiveAppointmentForecastRule {
    override fun check(item: AdaptiveAppointmentForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is stale")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentForecastNameLengthRule(var maxLength: Int) : AdaptiveAppointmentForecastRule {
    override fun check(item: AdaptiveAppointmentForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive appointment forecast. */
data class AdaptiveAppointmentForecastValidator(var rules: MutableList<AdaptiveAppointmentForecastRule>) {
    fun validate(item: AdaptiveAppointmentForecast): MutableList<String> {
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

    fun requireValid(item: AdaptiveAppointmentForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAppointmentForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAppointmentForecastValidator(): AdaptiveAppointmentForecastValidator {
    return AdaptiveAppointmentForecastValidator(
        mutableListOf<AdaptiveAppointmentForecastRule>(
            AdaptiveAppointmentForecastDurationMinutesRangeRule(3600, 3600),
            AdaptiveAppointmentForecastAllowedCarrierRule(mutableListOf<String>()),
            AdaptiveAppointmentForecastNameLengthRule(3600),
        ),
    )
}
