package com.acme.transport.safety.incident.reservation

/** A rule a incident preference must pass. Check returns its problems, or none. */
interface IncidentPreferenceRule {
    fun check(item: IncidentPreference): MutableList<String>
}

data class IncidentPreferenceCodePrefixRule(var prefix: String) : IncidentPreferenceRule {
    override fun check(item: IncidentPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentPreferenceWindowDaysRangeRule(var min: Int, var max: Int) : IncidentPreferenceRule {
    override fun check(item: IncidentPreference): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class IncidentPreferenceTrailerIdLengthRule(var maxLength: Int) : IncidentPreferenceRule {
    override fun check(item: IncidentPreference): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentPreferenceMaximumRangeRule(var min: Double, var max: Double) : IncidentPreferenceRule {
    override fun check(item: IncidentPreference): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentPreferenceRequiredCodeRule(var message: String) : IncidentPreferenceRule {
    override fun check(item: IncidentPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentPreferenceTitleLengthRule(var maxLength: Int) : IncidentPreferenceRule {
    override fun check(item: IncidentPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident preference. */
data class IncidentPreferenceValidator(var rules: MutableList<IncidentPreferenceRule>) {
    fun validate(item: IncidentPreference): MutableList<String> {
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

    fun requireValid(item: IncidentPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentPreferenceValidator(): IncidentPreferenceValidator {
    return IncidentPreferenceValidator(
        mutableListOf<IncidentPreferenceRule>(
            IncidentPreferenceCodePrefixRule("OPS"),
            IncidentPreferenceWindowDaysRangeRule(64, 255),
            IncidentPreferenceTrailerIdLengthRule(64),
            IncidentPreferenceMaximumRangeRule(1.2, 4128.4),
            IncidentPreferenceRequiredCodeRule("incident preference needs an entry"),
            IncidentPreferenceTitleLengthRule(120),
        ),
    )
}
