package com.acme.transport.terminal.appointment.metric

/** A rule a appointment adjustment must pass. Check returns its problems, or none. */
interface AppointmentAdjustmentRule {
    fun check(item: AppointmentAdjustment): MutableList<String>
}

data class AppointmentAdjustmentRequiredLabelRule(var message: String) : AppointmentAdjustmentRule {
    override fun check(item: AppointmentAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAdjustmentLabelLengthRule(var maxLength: Int) : AppointmentAdjustmentRule {
    override fun check(item: AppointmentAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAdjustmentExpiresAtRangeRule(var min: Int, var max: Int) : AppointmentAdjustmentRule {
    override fun check(item: AppointmentAdjustment): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment adjustment. */
data class AppointmentAdjustmentValidator(var rules: MutableList<AppointmentAdjustmentRule>) {
    fun validate(item: AppointmentAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentAdjustmentValidator(): AppointmentAdjustmentValidator {
    return AppointmentAdjustmentValidator(
        mutableListOf<AppointmentAdjustmentRule>(
            AppointmentAdjustmentRequiredLabelRule("appointment adjustment needs a value"),
            AppointmentAdjustmentLabelLengthRule(80),
            AppointmentAdjustmentExpiresAtRangeRule(120, 86400),
        ),
    )
}
