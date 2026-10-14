package com.acme.transport.emissions.emissionfactor.factorpreference

/** A rule a seasonal emission factor preference must pass. Check returns its problems, or none. */
interface SeasonalEmissionFactorPreferenceRule {
    fun check(item: SeasonalEmissionFactorPreference): MutableList<String>
}

data class SeasonalEmissionFactorPreferenceCodeLengthRule(
    var maxLength: Int,
) : SeasonalEmissionFactorPreferenceRule {
    override fun check(item: SeasonalEmissionFactorPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorPreferenceTitleLengthRule(
    var maxLength: Int,
) : SeasonalEmissionFactorPreferenceRule {
    override fun check(item: SeasonalEmissionFactorPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorPreferenceStopCountRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalEmissionFactorPreferenceRule {
    override fun check(item: SeasonalEmissionFactorPreference): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal emission factor preference. */
data class SeasonalEmissionFactorPreferenceValidator(
    var rules: MutableList<SeasonalEmissionFactorPreferenceRule>,
) {
    fun validate(item: SeasonalEmissionFactorPreference): MutableList<String> {
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

    fun requireValid(item: SeasonalEmissionFactorPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEmissionFactorPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEmissionFactorPreferenceValidator(): SeasonalEmissionFactorPreferenceValidator {
    return SeasonalEmissionFactorPreferenceValidator(
        mutableListOf<SeasonalEmissionFactorPreferenceRule>(
            SeasonalEmissionFactorPreferenceCodeLengthRule(80),
            SeasonalEmissionFactorPreferenceTitleLengthRule(255),
            SeasonalEmissionFactorPreferenceStopCountRangeRule(80, 86400),
        ),
    )
}
