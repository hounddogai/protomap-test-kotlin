package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.text.normalize

/** A rule a appointment draft must pass. Check returns its problems, or none. */
interface AppointmentDraftRule {
    fun check(item: AppointmentDraft): MutableList<String>
}

data class AppointmentDraftThresholdRangeRule(var min: Double, var max: Double) : AppointmentDraftRule {
    override fun check(item: AppointmentDraft): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class AppointmentDraftRequiredNameRule(var message: String) : AppointmentDraftRule {
    override fun check(item: AppointmentDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentDraftRequiredTitleRule(var message: String) : AppointmentDraftRule {
    override fun check(item: AppointmentDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentDraftAllowedExternalRefRule(var allowed: MutableList<String>) : AppointmentDraftRule {
    override fun check(item: AppointmentDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentDraftRequiredExternalRefRule(var message: String) : AppointmentDraftRule {
    override fun check(item: AppointmentDraft): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment draft. */
data class AppointmentDraftValidator(var rules: MutableList<AppointmentDraftRule>) {
    fun validate(item: AppointmentDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentDraftValidator(): AppointmentDraftValidator {
    return AppointmentDraftValidator(
        mutableListOf<AppointmentDraftRule>(
            AppointmentDraftThresholdRangeRule(1.4, 3526.4),
            AppointmentDraftRequiredNameRule("appointment draft needs an entry"),
            AppointmentDraftRequiredTitleRule("appointment draft needs this field"),
            AppointmentDraftAllowedExternalRefRule(mutableListOf<String>("fallback", "rolling")),
            AppointmentDraftRequiredExternalRefRule("appointment draft needs a value"),
        ),
    )
}
