package com.acme.transport.terminal.appointment.appointmentrollup

/** A rule a global appointment rollup must pass. Check returns its problems, or none. */
interface GlobalAppointmentRollupRule {
    fun check(item: GlobalAppointmentRollup): MutableList<String>
}

data class GlobalAppointmentRollupTitleLengthRule(var maxLength: Int) : GlobalAppointmentRollupRule {
    override fun check(item: GlobalAppointmentRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAppointmentRollupRequiredReviewerRule(var message: String) : GlobalAppointmentRollupRule {
    override fun check(item: GlobalAppointmentRollup): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAppointmentRollupApprovedGuardRule(var limit: Int) : GlobalAppointmentRollupRule {
    override fun check(item: GlobalAppointmentRollup): MutableList<String> {
        if (item.approved && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global appointment rollup. */
data class GlobalAppointmentRollupValidator(var rules: MutableList<GlobalAppointmentRollupRule>) {
    fun validate(item: GlobalAppointmentRollup): MutableList<String> {
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

    fun requireValid(item: GlobalAppointmentRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAppointmentRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAppointmentRollupValidator(): GlobalAppointmentRollupValidator {
    return GlobalAppointmentRollupValidator(
        mutableListOf<GlobalAppointmentRollupRule>(
            GlobalAppointmentRollupTitleLengthRule(86400),
            GlobalAppointmentRollupRequiredReviewerRule("global appointment rollup needs a value"),
            GlobalAppointmentRollupApprovedGuardRule(86400),
        ),
    )
}
