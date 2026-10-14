package com.acme.transport.terminal.appointment.threshold

/** A rule a appointment threshold must pass. Check returns its problems, or none. */
interface AppointmentThresholdRule {
    fun check(item: AppointmentThreshold): MutableList<String>
}

data class AppointmentThresholdTitleLengthRule(var maxLength: Int) : AppointmentThresholdRule {
    override fun check(item: AppointmentThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentThresholdEtaMinutesRangeRule(var min: Int, var max: Int) : AppointmentThresholdRule {
    override fun check(item: AppointmentThreshold): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class AppointmentThresholdCountryLengthRule(var maxLength: Int) : AppointmentThresholdRule {
    override fun check(item: AppointmentThreshold): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment threshold. */
data class AppointmentThresholdValidator(var rules: MutableList<AppointmentThresholdRule>) {
    fun validate(item: AppointmentThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentThresholdValidator(): AppointmentThresholdValidator {
    return AppointmentThresholdValidator(
        mutableListOf<AppointmentThresholdRule>(
            AppointmentThresholdTitleLengthRule(80),
            AppointmentThresholdEtaMinutesRangeRule(64, 255),
            AppointmentThresholdCountryLengthRule(16),
        ),
    )
}
