package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.text.normalize

/** A rule a legacy incident preference must pass. Check returns its problems, or none. */
interface LegacyIncidentPreferenceRule {
    fun check(item: LegacyIncidentPreference): MutableList<String>
}

data class LegacyIncidentPreferenceAllowedLabelRule(
    var allowed: MutableList<String>,
) : LegacyIncidentPreferenceRule {
    override fun check(item: LegacyIncidentPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentPreferenceLabelLengthRule(var maxLength: Int) : LegacyIncidentPreferenceRule {
    override fun check(item: LegacyIncidentPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentPreferenceTimezoneLengthRule(var maxLength: Int) : LegacyIncidentPreferenceRule {
    override fun check(item: LegacyIncidentPreference): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentPreferenceCodePrefixRule(var prefix: String) : LegacyIncidentPreferenceRule {
    override fun check(item: LegacyIncidentPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentPreferenceOdometerRangeRule(
    var min: Int,
    var max: Int,
) : LegacyIncidentPreferenceRule {
    override fun check(item: LegacyIncidentPreference): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy incident preference. */
data class LegacyIncidentPreferenceValidator(var rules: MutableList<LegacyIncidentPreferenceRule>) {
    fun validate(item: LegacyIncidentPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyIncidentPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyIncidentPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyIncidentPreferenceValidator(): LegacyIncidentPreferenceValidator {
    return LegacyIncidentPreferenceValidator(
        mutableListOf<LegacyIncidentPreferenceRule>(
            LegacyIncidentPreferenceAllowedLabelRule(mutableListOf<String>()),
            LegacyIncidentPreferenceLabelLengthRule(255),
            LegacyIncidentPreferenceTimezoneLengthRule(64),
            LegacyIncidentPreferenceCodePrefixRule("ACM"),
            LegacyIncidentPreferenceOdometerRangeRule(16, 16),
        ),
    )
}
