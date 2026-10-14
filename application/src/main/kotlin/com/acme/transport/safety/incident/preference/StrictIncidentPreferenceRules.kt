package com.acme.transport.safety.incident.preference

/** A rule a strict incident preference must pass. Check returns its problems, or none. */
interface StrictIncidentPreferenceRule {
    fun check(item: StrictIncidentPreference): MutableList<String>
}

data class StrictIncidentPreferenceCategoryLengthRule(var maxLength: Int) : StrictIncidentPreferenceRule {
    override fun check(item: StrictIncidentPreference): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentPreferenceRequiredCodeRule(var message: String) : StrictIncidentPreferenceRule {
    override fun check(item: StrictIncidentPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentPreferenceCodeLengthRule(var maxLength: Int) : StrictIncidentPreferenceRule {
    override fun check(item: StrictIncidentPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentPreferenceRequiredCategoryRule(var message: String) : StrictIncidentPreferenceRule {
    override fun check(item: StrictIncidentPreference): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict incident preference. */
data class StrictIncidentPreferenceValidator(var rules: MutableList<StrictIncidentPreferenceRule>) {
    fun validate(item: StrictIncidentPreference): MutableList<String> {
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

    fun requireValid(item: StrictIncidentPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictIncidentPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictIncidentPreferenceValidator(): StrictIncidentPreferenceValidator {
    return StrictIncidentPreferenceValidator(
        mutableListOf<StrictIncidentPreferenceRule>(
            StrictIncidentPreferenceCategoryLengthRule(255),
            StrictIncidentPreferenceRequiredCodeRule("strict incident preference needs a value"),
            StrictIncidentPreferenceCodeLengthRule(255),
            StrictIncidentPreferenceRequiredCategoryRule("strict incident preference needs this field"),
        ),
    )
}
