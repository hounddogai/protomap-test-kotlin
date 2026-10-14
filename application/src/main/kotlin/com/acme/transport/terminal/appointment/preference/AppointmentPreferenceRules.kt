package com.acme.transport.terminal.appointment.preference

import com.acme.transport.kit.text.normalize

/** A rule a appointment preference must pass. Check returns its problems, or none. */
interface AppointmentPreferenceRule {
    fun check(item: AppointmentPreference): MutableList<String>
}

data class AppointmentPreferenceAllowedCodeRule(var allowed: MutableList<String>) : AppointmentPreferenceRule {
    override fun check(item: AppointmentPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPreferenceBudgetRangeRule(var min: Double, var max: Double) : AppointmentPreferenceRule {
    override fun check(item: AppointmentPreference): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPreferenceChannelLengthRule(var maxLength: Int) : AppointmentPreferenceRule {
    override fun check(item: AppointmentPreference): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPreferenceLabelLengthRule(var maxLength: Int) : AppointmentPreferenceRule {
    override fun check(item: AppointmentPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPreferenceRequiredPhoneRule(var message: String) : AppointmentPreferenceRule {
    override fun check(item: AppointmentPreference): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPreferenceTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : AppointmentPreferenceRule {
    override fun check(item: AppointmentPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a appointment preference. */
data class AppointmentPreferenceValidator(var rules: MutableList<AppointmentPreferenceRule>) {
    fun validate(item: AppointmentPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentPreferenceValidator(): AppointmentPreferenceValidator {
    return AppointmentPreferenceValidator(
        mutableListOf<AppointmentPreferenceRule>(
            AppointmentPreferenceAllowedCodeRule(mutableListOf<String>("adaptive")),
            AppointmentPreferenceBudgetRangeRule(3.5, 4432.2),
            AppointmentPreferenceChannelLengthRule(64),
            AppointmentPreferenceLabelLengthRule(120),
            AppointmentPreferenceRequiredPhoneRule("appointment preference needs this field"),
            AppointmentPreferenceTagsLimitRule(32, mutableListOf<String>("strict", "manual", "default")),
        ),
    )
}
