package com.acme.transport.terminal.dwell.assignment

/** A rule a fallback dwell preference must pass. Check returns its problems, or none. */
interface FallbackDwellPreferenceRule {
    fun check(item: FallbackDwellPreference): MutableList<String>
}

data class FallbackDwellPreferenceNameLengthRule(var maxLength: Int) : FallbackDwellPreferenceRule {
    override fun check(item: FallbackDwellPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellPreferenceWeightRangeRule(
    var min: Double,
    var max: Double,
) : FallbackDwellPreferenceRule {
    override fun check(item: FallbackDwellPreference): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellPreferenceRequiredCodeRule(var message: String) : FallbackDwellPreferenceRule {
    override fun check(item: FallbackDwellPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dwell preference. */
data class FallbackDwellPreferenceValidator(var rules: MutableList<FallbackDwellPreferenceRule>) {
    fun validate(item: FallbackDwellPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDwellPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDwellPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDwellPreferenceValidator(): FallbackDwellPreferenceValidator {
    return FallbackDwellPreferenceValidator(
        mutableListOf<FallbackDwellPreferenceRule>(
            FallbackDwellPreferenceNameLengthRule(80),
            FallbackDwellPreferenceWeightRangeRule(5.7, 3714.8),
            FallbackDwellPreferenceRequiredCodeRule("fallback dwell preference needs an entry"),
        ),
    )
}
