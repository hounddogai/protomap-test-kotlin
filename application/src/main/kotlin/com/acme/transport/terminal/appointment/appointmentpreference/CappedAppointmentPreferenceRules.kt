package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.text.normalize

/** A rule a capped appointment preference must pass. Check returns its problems, or none. */
interface CappedAppointmentPreferenceRule {
    fun check(item: CappedAppointmentPreference): MutableList<String>
}

data class CappedAppointmentPreferenceRequiredLabelRule(var message: String) : CappedAppointmentPreferenceRule {
    override fun check(item: CappedAppointmentPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAppointmentPreferenceLabelLengthRule(var maxLength: Int) : CappedAppointmentPreferenceRule {
    override fun check(item: CappedAppointmentPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAppointmentPreferenceAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : CappedAppointmentPreferenceRule {
    override fun check(item: CappedAppointmentPreference): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class CappedAppointmentPreferenceAllowedCodeRule(
    var allowed: MutableList<String>,
) : CappedAppointmentPreferenceRule {
    override fun check(item: CappedAppointmentPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CappedAppointmentPreferenceCodePrefixRule(var prefix: String) : CappedAppointmentPreferenceRule {
    override fun check(item: CappedAppointmentPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped appointment preference. */
data class CappedAppointmentPreferenceValidator(var rules: MutableList<CappedAppointmentPreferenceRule>) {
    fun validate(item: CappedAppointmentPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedAppointmentPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAppointmentPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAppointmentPreferenceValidator(): CappedAppointmentPreferenceValidator {
    return CappedAppointmentPreferenceValidator(
        mutableListOf<CappedAppointmentPreferenceRule>(
            CappedAppointmentPreferenceRequiredLabelRule("capped appointment preference needs a value"),
            CappedAppointmentPreferenceLabelLengthRule(32),
            CappedAppointmentPreferenceAttemptsRangeRule(16, 32),
            CappedAppointmentPreferenceAllowedCodeRule(mutableListOf<String>("fallback", "strict", "batched")),
            CappedAppointmentPreferenceCodePrefixRule("REF"),
        ),
    )
}
