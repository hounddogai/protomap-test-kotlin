package com.acme.transport.terminal.appointment.rollup

/** A rule a appointment rollup must pass. Check returns its problems, or none. */
interface AppointmentRollupRule {
    fun check(item: AppointmentRollup): MutableList<String>
}

data class AppointmentRollupCodePrefixRule(var prefix: String) : AppointmentRollupRule {
    override fun check(item: AppointmentRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentRollupCodeLengthRule(var maxLength: Int) : AppointmentRollupRule {
    override fun check(item: AppointmentRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentRollupRequiredCodeRule(var message: String) : AppointmentRollupRule {
    override fun check(item: AppointmentRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment rollup. */
data class AppointmentRollupValidator(var rules: MutableList<AppointmentRollupRule>) {
    fun validate(item: AppointmentRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentRollupValidator(): AppointmentRollupValidator {
    return AppointmentRollupValidator(
        mutableListOf<AppointmentRollupRule>(
            AppointmentRollupCodePrefixRule("ACM"),
            AppointmentRollupCodeLengthRule(1000),
            AppointmentRollupRequiredCodeRule("appointment rollup needs this field"),
        ),
    )
}
