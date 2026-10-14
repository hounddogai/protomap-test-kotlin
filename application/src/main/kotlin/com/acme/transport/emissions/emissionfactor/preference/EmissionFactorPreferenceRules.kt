package com.acme.transport.emissions.emissionfactor.preference

/** A rule a emission factor preference must pass. Check returns its problems, or none. */
interface EmissionFactorPreferenceRule {
    fun check(item: EmissionFactorPreference): MutableList<String>
}

data class EmissionFactorPreferenceLabelLengthRule(var maxLength: Int) : EmissionFactorPreferenceRule {
    override fun check(item: EmissionFactorPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorPreferenceRequiredLabelRule(var message: String) : EmissionFactorPreferenceRule {
    override fun check(item: EmissionFactorPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorPreferenceLimitRangeRule(var min: Int, var max: Int) : EmissionFactorPreferenceRule {
    override fun check(item: EmissionFactorPreference): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor preference. */
data class EmissionFactorPreferenceValidator(var rules: MutableList<EmissionFactorPreferenceRule>) {
    fun validate(item: EmissionFactorPreference): MutableList<String> {
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

    fun requireValid(item: EmissionFactorPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorPreferenceValidator(): EmissionFactorPreferenceValidator {
    return EmissionFactorPreferenceValidator(
        mutableListOf<EmissionFactorPreferenceRule>(
            EmissionFactorPreferenceLabelLengthRule(80),
            EmissionFactorPreferenceRequiredLabelRule("emission factor preference needs an entry"),
            EmissionFactorPreferenceLimitRangeRule(32, 3600),
        ),
    )
}
