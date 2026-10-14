package com.acme.transport.terminal.appointment.exception

/** A rule a appointment exception must pass. Check returns its problems, or none. */
interface AppointmentExceptionRule {
    fun check(item: AppointmentException): MutableList<String>
}

data class AppointmentExceptionRequiredTitleRule(var message: String) : AppointmentExceptionRule {
    override fun check(item: AppointmentException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentExceptionTitleLengthRule(var maxLength: Int) : AppointmentExceptionRule {
    override fun check(item: AppointmentException): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentExceptionRateRangeRule(var min: Double, var max: Double) : AppointmentExceptionRule {
    override fun check(item: AppointmentException): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment exception. */
data class AppointmentExceptionValidator(var rules: MutableList<AppointmentExceptionRule>) {
    fun validate(item: AppointmentException): MutableList<String> {
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

    fun requireValid(item: AppointmentException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentException>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentExceptionValidator(): AppointmentExceptionValidator {
    return AppointmentExceptionValidator(
        mutableListOf<AppointmentExceptionRule>(
            AppointmentExceptionRequiredTitleRule("appointment exception needs an entry"),
            AppointmentExceptionTitleLengthRule(32),
            AppointmentExceptionRateRangeRule(6.2, 328.2),
        ),
    )
}
