package com.acme.transport.terminal.appointment.ledger

/** A rule a primary appointment preference must pass. Check returns its problems, or none. */
interface PrimaryAppointmentPreferenceRule {
    fun check(item: PrimaryAppointmentPreference): MutableList<String>
}

data class PrimaryAppointmentPreferenceRequiredCodeRule(
    var message: String,
) : PrimaryAppointmentPreferenceRule {
    override fun check(item: PrimaryAppointmentPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentPreferenceCodeLengthRule(var maxLength: Int) : PrimaryAppointmentPreferenceRule {
    override fun check(item: PrimaryAppointmentPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentPreferenceRevisionRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryAppointmentPreferenceRule {
    override fun check(item: PrimaryAppointmentPreference): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary appointment preference. */
data class PrimaryAppointmentPreferenceValidator(var rules: MutableList<PrimaryAppointmentPreferenceRule>) {
    fun validate(item: PrimaryAppointmentPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAppointmentPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAppointmentPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAppointmentPreferenceValidator(): PrimaryAppointmentPreferenceValidator {
    return PrimaryAppointmentPreferenceValidator(
        mutableListOf<PrimaryAppointmentPreferenceRule>(
            PrimaryAppointmentPreferenceRequiredCodeRule("primary appointment preference needs this field"),
            PrimaryAppointmentPreferenceCodeLengthRule(16),
            PrimaryAppointmentPreferenceRevisionRangeRule(16, 86400),
        ),
    )
}
