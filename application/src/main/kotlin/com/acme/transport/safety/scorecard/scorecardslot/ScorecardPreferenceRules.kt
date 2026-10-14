package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.normalize

/** A rule a scorecard preference must pass. Check returns its problems, or none. */
interface ScorecardPreferenceRule {
    fun check(item: ScorecardPreference): MutableList<String>
}

data class ScorecardPreferenceCodePrefixRule(var prefix: String) : ScorecardPreferenceRule {
    override fun check(item: ScorecardPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardPreferenceTitleLengthRule(var maxLength: Int) : ScorecardPreferenceRule {
    override fun check(item: ScorecardPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardPreferenceAllowedTitleRule(var allowed: MutableList<String>) : ScorecardPreferenceRule {
    override fun check(item: ScorecardPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardPreferenceWeightRangeRule(var min: Double, var max: Double) : ScorecardPreferenceRule {
    override fun check(item: ScorecardPreference): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardPreferenceRequiredSummaryRule(var message: String) : ScorecardPreferenceRule {
    override fun check(item: ScorecardPreference): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardPreferenceCapacityRangeRule(var min: Int, var max: Int) : ScorecardPreferenceRule {
    override fun check(item: ScorecardPreference): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard preference. */
data class ScorecardPreferenceValidator(var rules: MutableList<ScorecardPreferenceRule>) {
    fun validate(item: ScorecardPreference): MutableList<String> {
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

    fun requireValid(item: ScorecardPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardPreferenceValidator(): ScorecardPreferenceValidator {
    return ScorecardPreferenceValidator(
        mutableListOf<ScorecardPreferenceRule>(
            ScorecardPreferenceCodePrefixRule("Q"),
            ScorecardPreferenceTitleLengthRule(3600),
            ScorecardPreferenceAllowedTitleRule(mutableListOf<String>("lenient", "tiered")),
            ScorecardPreferenceWeightRangeRule(9.7, 1463.2),
            ScorecardPreferenceRequiredSummaryRule("scorecard preference needs an entry"),
            ScorecardPreferenceCapacityRangeRule(80, 1000),
        ),
    )
}
