package com.acme.transport.terminal.appointment.reservation

/** A rule a appointment reservation must pass. Check returns its problems, or none. */
interface AppointmentReservationRule {
    fun check(item: AppointmentReservation): MutableList<String>
}

data class AppointmentReservationRequiredTitleRule(var message: String) : AppointmentReservationRule {
    override fun check(item: AppointmentReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentReservationCodePrefixRule(var prefix: String) : AppointmentReservationRule {
    override fun check(item: AppointmentReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentReservationCodeLengthRule(var maxLength: Int) : AppointmentReservationRule {
    override fun check(item: AppointmentReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment reservation. */
data class AppointmentReservationValidator(var rules: MutableList<AppointmentReservationRule>) {
    fun validate(item: AppointmentReservation): MutableList<String> {
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

    fun requireValid(item: AppointmentReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentReservationValidator(): AppointmentReservationValidator {
    return AppointmentReservationValidator(
        mutableListOf<AppointmentReservationRule>(
            AppointmentReservationRequiredTitleRule("appointment reservation needs an entry"),
            AppointmentReservationCodePrefixRule("ACM"),
            AppointmentReservationCodeLengthRule(86400),
        ),
    )
}
