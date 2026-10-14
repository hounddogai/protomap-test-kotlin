package com.acme.transport.terminal.appointment.appointmentscore

/** A rule a scheduled appointment forecast must pass. Check returns its problems, or none. */
interface ScheduledAppointmentForecastRule {
    fun check(item: ScheduledAppointmentForecast): MutableList<String>
}

data class ScheduledAppointmentForecastCodeLengthRule(var maxLength: Int) : ScheduledAppointmentForecastRule {
    override fun check(item: ScheduledAppointmentForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentForecastTrailerIdLengthRule(
    var maxLength: Int,
) : ScheduledAppointmentForecastRule {
    override fun check(item: ScheduledAppointmentForecast): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentForecastFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledAppointmentForecastRule {
    override fun check(item: ScheduledAppointmentForecast): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ScheduledAppointmentForecastContactEmailLengthRule(
    var maxLength: Int,
) : ScheduledAppointmentForecastRule {
    override fun check(item: ScheduledAppointmentForecast): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentForecastRequiredTitleRule(
    var message: String,
) : ScheduledAppointmentForecastRule {
    override fun check(item: ScheduledAppointmentForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled appointment forecast. */
data class ScheduledAppointmentForecastValidator(var rules: MutableList<ScheduledAppointmentForecastRule>) {
    fun validate(item: ScheduledAppointmentForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAppointmentForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAppointmentForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAppointmentForecastValidator(): ScheduledAppointmentForecastValidator {
    return ScheduledAppointmentForecastValidator(
        mutableListOf<ScheduledAppointmentForecastRule>(
            ScheduledAppointmentForecastCodeLengthRule(3600),
            ScheduledAppointmentForecastTrailerIdLengthRule(80),
            ScheduledAppointmentForecastFuelLitersRangeRule(4.3, 263.5),
            ScheduledAppointmentForecastContactEmailLengthRule(16),
            ScheduledAppointmentForecastRequiredTitleRule("scheduled appointment forecast needs this field"),
        ),
    )
}
