package com.acme.transport.terminal.appointment.allocation

/** A rule a appointment window must pass. Check returns its problems, or none. */
interface AppointmentWindowRule {
    fun check(item: AppointmentWindow): MutableList<String>
}

data class AppointmentWindowRequiredNotesRule(var message: String) : AppointmentWindowRule {
    override fun check(item: AppointmentWindow): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentWindowDurationMinutesRangeRule(var min: Int, var max: Int) : AppointmentWindowRule {
    override fun check(item: AppointmentWindow): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class AppointmentWindowCodePrefixRule(var prefix: String) : AppointmentWindowRule {
    override fun check(item: AppointmentWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentWindowRequiredShiftCodeRule(var message: String) : AppointmentWindowRule {
    override fun check(item: AppointmentWindow): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment window. */
data class AppointmentWindowValidator(var rules: MutableList<AppointmentWindowRule>) {
    fun validate(item: AppointmentWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentWindowValidator(): AppointmentWindowValidator {
    return AppointmentWindowValidator(
        mutableListOf<AppointmentWindowRule>(
            AppointmentWindowRequiredNotesRule("appointment window needs an entry"),
            AppointmentWindowDurationMinutesRangeRule(64, 86400),
            AppointmentWindowCodePrefixRule("X"),
            AppointmentWindowRequiredShiftCodeRule("appointment window needs an entry"),
        ),
    )
}
