package com.acme.transport.terminal.appointment.allocation

/** A rule a appointment claim must pass. Check returns its problems, or none. */
interface AppointmentClaimRule {
    fun check(item: AppointmentClaim): MutableList<String>
}

data class AppointmentClaimRequiredTitleRule(var message: String) : AppointmentClaimRule {
    override fun check(item: AppointmentClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentClaimDriverIdLengthRule(var maxLength: Int) : AppointmentClaimRule {
    override fun check(item: AppointmentClaim): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentClaimCategoryLengthRule(var maxLength: Int) : AppointmentClaimRule {
    override fun check(item: AppointmentClaim): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment claim. */
data class AppointmentClaimValidator(var rules: MutableList<AppointmentClaimRule>) {
    fun validate(item: AppointmentClaim): MutableList<String> {
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

    fun requireValid(item: AppointmentClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentClaimValidator(): AppointmentClaimValidator {
    return AppointmentClaimValidator(
        mutableListOf<AppointmentClaimRule>(
            AppointmentClaimRequiredTitleRule("appointment claim needs an entry"),
            AppointmentClaimDriverIdLengthRule(1000),
            AppointmentClaimCategoryLengthRule(80),
        ),
    )
}
