package com.acme.transport.terminal.appointment.allocation

/** A rule a scheduled appointment estimate must pass. Check returns its problems, or none. */
interface ScheduledAppointmentEstimateRule {
    fun check(item: ScheduledAppointmentEstimate): MutableList<String>
}

data class ScheduledAppointmentEstimateLocaleLengthRule(var maxLength: Int) : ScheduledAppointmentEstimateRule {
    override fun check(item: ScheduledAppointmentEstimate): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentEstimateRequiredLocaleRule(
    var message: String,
) : ScheduledAppointmentEstimateRule {
    override fun check(item: ScheduledAppointmentEstimate): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentEstimateRequiredCodeRule(
    var message: String,
) : ScheduledAppointmentEstimateRule {
    override fun check(item: ScheduledAppointmentEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentEstimateStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledAppointmentEstimateRule {
    override fun check(item: ScheduledAppointmentEstimate): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class ScheduledAppointmentEstimateCodeLengthRule(var maxLength: Int) : ScheduledAppointmentEstimateRule {
    override fun check(item: ScheduledAppointmentEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled appointment estimate. */
data class ScheduledAppointmentEstimateValidator(var rules: MutableList<ScheduledAppointmentEstimateRule>) {
    fun validate(item: ScheduledAppointmentEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAppointmentEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAppointmentEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAppointmentEstimateValidator(): ScheduledAppointmentEstimateValidator {
    return ScheduledAppointmentEstimateValidator(
        mutableListOf<ScheduledAppointmentEstimateRule>(
            ScheduledAppointmentEstimateLocaleLengthRule(80),
            ScheduledAppointmentEstimateRequiredLocaleRule("scheduled appointment estimate needs an entry"),
            ScheduledAppointmentEstimateRequiredCodeRule("scheduled appointment estimate needs an entry"),
            ScheduledAppointmentEstimateStartsAtRangeRule(64, 80),
            ScheduledAppointmentEstimateCodeLengthRule(80),
        ),
    )
}
