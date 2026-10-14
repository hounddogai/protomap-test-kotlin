package com.acme.transport.terminal.appointment.appointmenttemplate

/** A rule a rolling appointment exception must pass. Check returns its problems, or none. */
interface RollingAppointmentExceptionRule {
    fun check(item: RollingAppointmentException): MutableList<String>
}

data class RollingAppointmentExceptionRequiredLabelRule(var message: String) : RollingAppointmentExceptionRule {
    override fun check(item: RollingAppointmentException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentExceptionCodePrefixRule(var prefix: String) : RollingAppointmentExceptionRule {
    override fun check(item: RollingAppointmentException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentExceptionRequiredTrailerIdRule(
    var message: String,
) : RollingAppointmentExceptionRule {
    override fun check(item: RollingAppointmentException): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentExceptionPalletsRangeRule(
    var min: Int,
    var max: Int,
) : RollingAppointmentExceptionRule {
    override fun check(item: RollingAppointmentException): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentExceptionTrailerIdLengthRule(
    var maxLength: Int,
) : RollingAppointmentExceptionRule {
    override fun check(item: RollingAppointmentException): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentExceptionRequiredCodeRule(var message: String) : RollingAppointmentExceptionRule {
    override fun check(item: RollingAppointmentException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling appointment exception. */
data class RollingAppointmentExceptionValidator(var rules: MutableList<RollingAppointmentExceptionRule>) {
    fun validate(item: RollingAppointmentException): MutableList<String> {
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

    fun requireValid(item: RollingAppointmentException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAppointmentException>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAppointmentExceptionValidator(): RollingAppointmentExceptionValidator {
    return RollingAppointmentExceptionValidator(
        mutableListOf<RollingAppointmentExceptionRule>(
            RollingAppointmentExceptionRequiredLabelRule("rolling appointment exception needs this field"),
            RollingAppointmentExceptionCodePrefixRule("ACM"),
            RollingAppointmentExceptionRequiredTrailerIdRule("rolling appointment exception needs an entry"),
            RollingAppointmentExceptionPalletsRangeRule(255, 255),
            RollingAppointmentExceptionTrailerIdLengthRule(86400),
            RollingAppointmentExceptionRequiredCodeRule("rolling appointment exception needs an entry"),
        ),
    )
}
