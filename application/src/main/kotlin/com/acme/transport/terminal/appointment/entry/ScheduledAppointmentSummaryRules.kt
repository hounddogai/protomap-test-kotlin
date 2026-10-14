package com.acme.transport.terminal.appointment.entry

/** A rule a scheduled appointment summary must pass. Check returns its problems, or none. */
interface ScheduledAppointmentSummaryRule {
    fun check(item: ScheduledAppointmentSummary): MutableList<String>
}

data class ScheduledAppointmentSummaryWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledAppointmentSummaryRule {
    override fun check(item: ScheduledAppointmentSummary): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentSummaryRequiredCategoryRule(
    var message: String,
) : ScheduledAppointmentSummaryRule {
    override fun check(item: ScheduledAppointmentSummary): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentSummaryCodeLengthRule(var maxLength: Int) : ScheduledAppointmentSummaryRule {
    override fun check(item: ScheduledAppointmentSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentSummaryRequiredLabelRule(var message: String) : ScheduledAppointmentSummaryRule {
    override fun check(item: ScheduledAppointmentSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAppointmentSummaryRequiredCodeRule(var message: String) : ScheduledAppointmentSummaryRule {
    override fun check(item: ScheduledAppointmentSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled appointment summary. */
data class ScheduledAppointmentSummaryValidator(var rules: MutableList<ScheduledAppointmentSummaryRule>) {
    fun validate(item: ScheduledAppointmentSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAppointmentSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAppointmentSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAppointmentSummaryValidator(): ScheduledAppointmentSummaryValidator {
    return ScheduledAppointmentSummaryValidator(
        mutableListOf<ScheduledAppointmentSummaryRule>(
            ScheduledAppointmentSummaryWindowDaysRangeRule(32, 32),
            ScheduledAppointmentSummaryRequiredCategoryRule("scheduled appointment summary needs an entry"),
            ScheduledAppointmentSummaryCodeLengthRule(1000),
            ScheduledAppointmentSummaryRequiredLabelRule("scheduled appointment summary needs an entry"),
            ScheduledAppointmentSummaryRequiredCodeRule("scheduled appointment summary needs an entry"),
        ),
    )
}
