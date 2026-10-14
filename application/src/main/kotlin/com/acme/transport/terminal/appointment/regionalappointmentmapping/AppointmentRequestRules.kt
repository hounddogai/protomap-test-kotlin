package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.text.normalize

/** A rule a appointment request must pass. Check returns its problems, or none. */
interface AppointmentRequestRule {
    fun check(item: AppointmentRequest): MutableList<String>
}

data class AppointmentRequestCodePrefixRule(var prefix: String) : AppointmentRequestRule {
    override fun check(item: AppointmentRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentRequestDepotLengthRule(var maxLength: Int) : AppointmentRequestRule {
    override fun check(item: AppointmentRequest): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentRequestAllowedNotesRule(var allowed: MutableList<String>) : AppointmentRequestRule {
    override fun check(item: AppointmentRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentRequestCodeLengthRule(var maxLength: Int) : AppointmentRequestRule {
    override fun check(item: AppointmentRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment request. */
data class AppointmentRequestValidator(var rules: MutableList<AppointmentRequestRule>) {
    fun validate(item: AppointmentRequest): MutableList<String> {
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

    fun requireValid(item: AppointmentRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentRequestValidator(): AppointmentRequestValidator {
    return AppointmentRequestValidator(
        mutableListOf<AppointmentRequestRule>(
            AppointmentRequestCodePrefixRule("X"),
            AppointmentRequestDepotLengthRule(32),
            AppointmentRequestAllowedNotesRule(mutableListOf<String>("scheduled", "tiered", "tiered")),
            AppointmentRequestCodeLengthRule(120),
        ),
    )
}
