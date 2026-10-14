package com.acme.transport.terminal.appointment.reservation

import com.acme.transport.kit.text.normalize

/** A rule a appointment target must pass. Check returns its problems, or none. */
interface AppointmentTargetRule {
    fun check(item: AppointmentTarget): MutableList<String>
}

data class AppointmentTargetAllowedCodeRule(var allowed: MutableList<String>) : AppointmentTargetRule {
    override fun check(item: AppointmentTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentTargetCeilingRangeRule(var min: Double, var max: Double) : AppointmentTargetRule {
    override fun check(item: AppointmentTarget): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentTargetRequiredCodeRule(var message: String) : AppointmentTargetRule {
    override fun check(item: AppointmentTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentTargetRequiredPhoneRule(var message: String) : AppointmentTargetRule {
    override fun check(item: AppointmentTarget): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentTargetCodePrefixRule(var prefix: String) : AppointmentTargetRule {
    override fun check(item: AppointmentTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentTargetPhoneLengthRule(var maxLength: Int) : AppointmentTargetRule {
    override fun check(item: AppointmentTarget): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment target. */
data class AppointmentTargetValidator(var rules: MutableList<AppointmentTargetRule>) {
    fun validate(item: AppointmentTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentTargetValidator(): AppointmentTargetValidator {
    return AppointmentTargetValidator(
        mutableListOf<AppointmentTargetRule>(
            AppointmentTargetAllowedCodeRule(mutableListOf<String>("weighted", "lenient", "primary")),
            AppointmentTargetCeilingRangeRule(9.8, 1289.4),
            AppointmentTargetRequiredCodeRule("appointment target needs a value"),
            AppointmentTargetRequiredPhoneRule("appointment target needs a value"),
            AppointmentTargetCodePrefixRule("ACM"),
            AppointmentTargetPhoneLengthRule(255),
        ),
    )
}
