package com.acme.transport.terminal.appointment.window

/** A rule a appointment rule must pass. Check returns its problems, or none. */
interface AppointmentRuleRule {
    fun check(item: AppointmentRule): MutableList<String>
}

data class AppointmentRuleRequiredDriverIdRule(var message: String) : AppointmentRuleRule {
    override fun check(item: AppointmentRule): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentRuleRequiredNameRule(var message: String) : AppointmentRuleRule {
    override fun check(item: AppointmentRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentRuleCodeLengthRule(var maxLength: Int) : AppointmentRuleRule {
    override fun check(item: AppointmentRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentRuleCodePrefixRule(var prefix: String) : AppointmentRuleRule {
    override fun check(item: AppointmentRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment rule. */
data class AppointmentRuleValidator(var rules: MutableList<AppointmentRuleRule>) {
    fun validate(item: AppointmentRule): MutableList<String> {
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

    fun requireValid(item: AppointmentRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentRule>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentRuleValidator(): AppointmentRuleValidator {
    return AppointmentRuleValidator(
        mutableListOf<AppointmentRuleRule>(
            AppointmentRuleRequiredDriverIdRule("appointment rule needs this field"),
            AppointmentRuleRequiredNameRule("appointment rule needs an entry"),
            AppointmentRuleCodeLengthRule(1000),
            AppointmentRuleCodePrefixRule("X"),
        ),
    )
}
