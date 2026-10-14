package com.acme.transport.terminal.appointment.appointmentrollup

import com.acme.transport.kit.text.normalize

/** A rule a appointment entry must pass. Check returns its problems, or none. */
interface AppointmentEntryRule {
    fun check(item: AppointmentEntry): MutableList<String>
}

data class AppointmentEntryRequiredCodeRule(var message: String) : AppointmentEntryRule {
    override fun check(item: AppointmentEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentEntryCodePrefixRule(var prefix: String) : AppointmentEntryRule {
    override fun check(item: AppointmentEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentEntryAllowedCodeRule(var allowed: MutableList<String>) : AppointmentEntryRule {
    override fun check(item: AppointmentEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment entry. */
data class AppointmentEntryValidator(var rules: MutableList<AppointmentEntryRule>) {
    fun validate(item: AppointmentEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentEntryValidator(): AppointmentEntryValidator {
    return AppointmentEntryValidator(
        mutableListOf<AppointmentEntryRule>(
            AppointmentEntryRequiredCodeRule("appointment entry needs a value"),
            AppointmentEntryCodePrefixRule("Q"),
            AppointmentEntryAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
