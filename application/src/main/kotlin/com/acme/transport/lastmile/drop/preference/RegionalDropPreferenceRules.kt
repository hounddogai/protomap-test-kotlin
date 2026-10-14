package com.acme.transport.lastmile.drop.preference

/** A rule a regional drop preference must pass. Check returns its problems, or none. */
interface RegionalDropPreferenceRule {
    fun check(item: RegionalDropPreference): MutableList<String>
}

data class RegionalDropPreferenceRequiredLabelRule(var message: String) : RegionalDropPreferenceRule {
    override fun check(item: RegionalDropPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropPreferenceLimitRangeRule(var min: Int, var max: Int) : RegionalDropPreferenceRule {
    override fun check(item: RegionalDropPreference): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class RegionalDropPreferenceCodePrefixRule(var prefix: String) : RegionalDropPreferenceRule {
    override fun check(item: RegionalDropPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropPreferenceLabelLengthRule(var maxLength: Int) : RegionalDropPreferenceRule {
    override fun check(item: RegionalDropPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional drop preference. */
data class RegionalDropPreferenceValidator(var rules: MutableList<RegionalDropPreferenceRule>) {
    fun validate(item: RegionalDropPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDropPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDropPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDropPreferenceValidator(): RegionalDropPreferenceValidator {
    return RegionalDropPreferenceValidator(
        mutableListOf<RegionalDropPreferenceRule>(
            RegionalDropPreferenceRequiredLabelRule("regional drop preference needs a value"),
            RegionalDropPreferenceLimitRangeRule(86400, 86400),
            RegionalDropPreferenceCodePrefixRule("REF"),
            RegionalDropPreferenceLabelLengthRule(1000),
        ),
    )
}
