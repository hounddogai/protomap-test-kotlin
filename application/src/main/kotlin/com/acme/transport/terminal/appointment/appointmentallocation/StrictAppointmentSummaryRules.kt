package com.acme.transport.terminal.appointment.appointmentallocation

/** A rule a strict appointment summary must pass. Check returns its problems, or none. */
interface StrictAppointmentSummaryRule {
    fun check(item: StrictAppointmentSummary): MutableList<String>
}

data class StrictAppointmentSummaryRequiredTitleRule(var message: String) : StrictAppointmentSummaryRule {
    override fun check(item: StrictAppointmentSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAppointmentSummaryWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : StrictAppointmentSummaryRule {
    override fun check(item: StrictAppointmentSummary): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class StrictAppointmentSummaryTitleLengthRule(var maxLength: Int) : StrictAppointmentSummaryRule {
    override fun check(item: StrictAppointmentSummary): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict appointment summary. */
data class StrictAppointmentSummaryValidator(var rules: MutableList<StrictAppointmentSummaryRule>) {
    fun validate(item: StrictAppointmentSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictAppointmentSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAppointmentSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAppointmentSummaryValidator(): StrictAppointmentSummaryValidator {
    return StrictAppointmentSummaryValidator(
        mutableListOf<StrictAppointmentSummaryRule>(
            StrictAppointmentSummaryRequiredTitleRule("strict appointment summary needs a value"),
            StrictAppointmentSummaryWindowDaysRangeRule(16, 80),
            StrictAppointmentSummaryTitleLengthRule(255),
        ),
    )
}
