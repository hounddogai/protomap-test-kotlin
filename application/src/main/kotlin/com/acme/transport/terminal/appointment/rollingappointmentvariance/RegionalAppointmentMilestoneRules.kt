package com.acme.transport.terminal.appointment.rollingappointmentvariance

/** A rule a regional appointment milestone must pass. Check returns its problems, or none. */
interface RegionalAppointmentMilestoneRule {
    fun check(item: RegionalAppointmentMilestone): MutableList<String>
}

data class RegionalAppointmentMilestoneRequiredTrailerIdRule(
    var message: String,
) : RegionalAppointmentMilestoneRule {
    override fun check(item: RegionalAppointmentMilestone): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAppointmentMilestoneTitleLengthRule(var maxLength: Int) : RegionalAppointmentMilestoneRule {
    override fun check(item: RegionalAppointmentMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAppointmentMilestoneCodeLengthRule(var maxLength: Int) : RegionalAppointmentMilestoneRule {
    override fun check(item: RegionalAppointmentMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional appointment milestone. */
data class RegionalAppointmentMilestoneValidator(var rules: MutableList<RegionalAppointmentMilestoneRule>) {
    fun validate(item: RegionalAppointmentMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalAppointmentMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAppointmentMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAppointmentMilestoneValidator(): RegionalAppointmentMilestoneValidator {
    return RegionalAppointmentMilestoneValidator(
        mutableListOf<RegionalAppointmentMilestoneRule>(
            RegionalAppointmentMilestoneRequiredTrailerIdRule("regional appointment milestone needs a value"),
            RegionalAppointmentMilestoneTitleLengthRule(16),
            RegionalAppointmentMilestoneCodeLengthRule(16),
        ),
    )
}
