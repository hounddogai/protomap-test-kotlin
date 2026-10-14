package com.acme.transport.network.transittime.preference

/** A rule a adaptive transit time preference must pass. Check returns its problems, or none. */
interface AdaptiveTransitTimePreferenceRule {
    fun check(item: AdaptiveTransitTimePreference): MutableList<String>
}

data class AdaptiveTransitTimePreferenceRequiredUnitRule(
    var message: String,
) : AdaptiveTransitTimePreferenceRule {
    override fun check(item: AdaptiveTransitTimePreference): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTransitTimePreferenceLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveTransitTimePreferenceRule {
    override fun check(item: AdaptiveTransitTimePreference): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTransitTimePreferenceCodeLengthRule(var maxLength: Int) : AdaptiveTransitTimePreferenceRule {
    override fun check(item: AdaptiveTransitTimePreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTransitTimePreferenceRequiredSourceRule(
    var message: String,
) : AdaptiveTransitTimePreferenceRule {
    override fun check(item: AdaptiveTransitTimePreference): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTransitTimePreferenceMaximumRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveTransitTimePreferenceRule {
    override fun check(item: AdaptiveTransitTimePreference): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTransitTimePreferenceUnitLengthRule(var maxLength: Int) : AdaptiveTransitTimePreferenceRule {
    override fun check(item: AdaptiveTransitTimePreference): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive transit time preference. */
data class AdaptiveTransitTimePreferenceValidator(var rules: MutableList<AdaptiveTransitTimePreferenceRule>) {
    fun validate(item: AdaptiveTransitTimePreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveTransitTimePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTransitTimePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTransitTimePreferenceValidator(): AdaptiveTransitTimePreferenceValidator {
    return AdaptiveTransitTimePreferenceValidator(
        mutableListOf<AdaptiveTransitTimePreferenceRule>(
            AdaptiveTransitTimePreferenceRequiredUnitRule("adaptive transit time preference needs this field"),
            AdaptiveTransitTimePreferenceLoadKgRangeRule(3.1, 3453.0),
            AdaptiveTransitTimePreferenceCodeLengthRule(16),
            AdaptiveTransitTimePreferenceRequiredSourceRule("adaptive transit time preference needs a value"),
            AdaptiveTransitTimePreferenceMaximumRangeRule(1.9, 3608.7),
            AdaptiveTransitTimePreferenceUnitLengthRule(255),
        ),
    )
}
