package com.acme.transport.terminal.appointment.mapping

/** A rule a appointment mapping must pass. Check returns its problems, or none. */
interface AppointmentMappingRule {
    fun check(item: AppointmentMapping): MutableList<String>
}

data class AppointmentMappingRequiredCodeRule(var message: String) : AppointmentMappingRule {
    override fun check(item: AppointmentMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentMappingRequiredTitleRule(var message: String) : AppointmentMappingRule {
    override fun check(item: AppointmentMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentMappingCodeLengthRule(var maxLength: Int) : AppointmentMappingRule {
    override fun check(item: AppointmentMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment mapping. */
data class AppointmentMappingValidator(var rules: MutableList<AppointmentMappingRule>) {
    fun validate(item: AppointmentMapping): MutableList<String> {
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

    fun requireValid(item: AppointmentMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentMappingValidator(): AppointmentMappingValidator {
    return AppointmentMappingValidator(
        mutableListOf<AppointmentMappingRule>(
            AppointmentMappingRequiredCodeRule("appointment mapping needs an entry"),
            AppointmentMappingRequiredTitleRule("appointment mapping needs an entry"),
            AppointmentMappingCodeLengthRule(255),
        ),
    )
}
