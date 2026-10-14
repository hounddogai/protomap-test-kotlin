package com.acme.transport.terminal.appointment.appointmentmapping

/** A rule a scheduled appointment mapping must pass. Check returns its problems, or none. */
interface ScheduledAppointmentMappingRule {
    fun check(item: ScheduledAppointmentMapping): MutableList<String>
}

data class ScheduledAppointmentMappingRequiredCodeRule(var message: String) : ScheduledAppointmentMappingRule {
    override fun check(item: ScheduledAppointmentMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentMappingOdometerRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledAppointmentMappingRule {
    override fun check(item: ScheduledAppointmentMapping): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class ScheduledAppointmentMappingCodeLengthRule(var maxLength: Int) : ScheduledAppointmentMappingRule {
    override fun check(item: ScheduledAppointmentMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentMappingSequenceRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledAppointmentMappingRule {
    override fun check(item: ScheduledAppointmentMapping): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled appointment mapping. */
data class ScheduledAppointmentMappingValidator(var rules: MutableList<ScheduledAppointmentMappingRule>) {
    fun validate(item: ScheduledAppointmentMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAppointmentMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAppointmentMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAppointmentMappingValidator(): ScheduledAppointmentMappingValidator {
    return ScheduledAppointmentMappingValidator(
        mutableListOf<ScheduledAppointmentMappingRule>(
            ScheduledAppointmentMappingRequiredCodeRule("scheduled appointment mapping needs this field"),
            ScheduledAppointmentMappingOdometerRangeRule(16, 86400),
            ScheduledAppointmentMappingCodeLengthRule(32),
            ScheduledAppointmentMappingSequenceRangeRule(255, 86400),
        ),
    )
}
