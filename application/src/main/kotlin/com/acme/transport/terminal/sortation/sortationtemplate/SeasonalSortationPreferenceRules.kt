package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.text.normalize

/** A rule a seasonal sortation preference must pass. Check returns its problems, or none. */
interface SeasonalSortationPreferenceRule {
    fun check(item: SeasonalSortationPreference): MutableList<String>
}

data class SeasonalSortationPreferenceLabelLengthRule(var maxLength: Int) : SeasonalSortationPreferenceRule {
    override fun check(item: SeasonalSortationPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationPreferenceSourceLengthRule(var maxLength: Int) : SeasonalSortationPreferenceRule {
    override fun check(item: SeasonalSortationPreference): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationPreferenceAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalSortationPreferenceRule {
    override fun check(item: SeasonalSortationPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationPreferenceQuantityRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalSortationPreferenceRule {
    override fun check(item: SeasonalSortationPreference): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationPreferenceCodeLengthRule(var maxLength: Int) : SeasonalSortationPreferenceRule {
    override fun check(item: SeasonalSortationPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal sortation preference. */
data class SeasonalSortationPreferenceValidator(var rules: MutableList<SeasonalSortationPreferenceRule>) {
    fun validate(item: SeasonalSortationPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalSortationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalSortationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalSortationPreferenceValidator(): SeasonalSortationPreferenceValidator {
    return SeasonalSortationPreferenceValidator(
        mutableListOf<SeasonalSortationPreferenceRule>(
            SeasonalSortationPreferenceLabelLengthRule(64),
            SeasonalSortationPreferenceSourceLengthRule(64),
            SeasonalSortationPreferenceAllowedCodeRule(mutableListOf<String>()),
            SeasonalSortationPreferenceQuantityRangeRule(64, 3600),
            SeasonalSortationPreferenceCodeLengthRule(16),
        ),
    )
}
