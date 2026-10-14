package com.acme.transport.terminal.appointment.appointmentpreference

/** A rule a appointment digest must pass. Check returns its problems, or none. */
interface AppointmentDigestRule {
    fun check(item: AppointmentDigest): MutableList<String>
}

data class AppointmentDigestCodePrefixRule(var prefix: String) : AppointmentDigestRule {
    override fun check(item: AppointmentDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentDigestSequenceRangeRule(var min: Int, var max: Int) : AppointmentDigestRule {
    override fun check(item: AppointmentDigest): MutableList<String> {
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

data class AppointmentDigestRequiredNameRule(var message: String) : AppointmentDigestRule {
    override fun check(item: AppointmentDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment digest. */
data class AppointmentDigestValidator(var rules: MutableList<AppointmentDigestRule>) {
    fun validate(item: AppointmentDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentDigestValidator(): AppointmentDigestValidator {
    return AppointmentDigestValidator(
        mutableListOf<AppointmentDigestRule>(
            AppointmentDigestCodePrefixRule("Q"),
            AppointmentDigestSequenceRangeRule(255, 255),
            AppointmentDigestRequiredNameRule("appointment digest needs an entry"),
        ),
    )
}
