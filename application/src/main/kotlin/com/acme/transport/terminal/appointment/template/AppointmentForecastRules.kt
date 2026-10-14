package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.text.normalize

/** A rule a appointment forecast must pass. Check returns its problems, or none. */
interface AppointmentForecastRule {
    fun check(item: AppointmentForecast): MutableList<String>
}

data class AppointmentForecastLabelLengthRule(var maxLength: Int) : AppointmentForecastRule {
    override fun check(item: AppointmentForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentForecastSequenceRangeRule(var min: Int, var max: Int) : AppointmentForecastRule {
    override fun check(item: AppointmentForecast): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentForecastCodeLengthRule(var maxLength: Int) : AppointmentForecastRule {
    override fun check(item: AppointmentForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentForecastAllowedLabelRule(var allowed: MutableList<String>) : AppointmentForecastRule {
    override fun check(item: AppointmentForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment forecast. */
data class AppointmentForecastValidator(var rules: MutableList<AppointmentForecastRule>) {
    fun validate(item: AppointmentForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentForecastValidator(): AppointmentForecastValidator {
    return AppointmentForecastValidator(
        mutableListOf<AppointmentForecastRule>(
            AppointmentForecastLabelLengthRule(86400),
            AppointmentForecastSequenceRangeRule(64, 255),
            AppointmentForecastCodeLengthRule(86400),
            AppointmentForecastAllowedLabelRule(mutableListOf<String>("pending", "manual")),
        ),
    )
}
