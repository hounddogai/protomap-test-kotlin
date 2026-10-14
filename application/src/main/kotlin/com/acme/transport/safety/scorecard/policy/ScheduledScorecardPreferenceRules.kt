package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.text.normalize

/** A rule a scheduled scorecard preference must pass. Check returns its problems, or none. */
interface ScheduledScorecardPreferenceRule {
    fun check(item: ScheduledScorecardPreference): MutableList<String>
}

data class ScheduledScorecardPreferenceAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : ScheduledScorecardPreferenceRule {
    override fun check(item: ScheduledScorecardPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledScorecardPreferenceLabelLengthRule(var maxLength: Int) : ScheduledScorecardPreferenceRule {
    override fun check(item: ScheduledScorecardPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledScorecardPreferenceRatioRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledScorecardPreferenceRule {
    override fun check(item: ScheduledScorecardPreference): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class ScheduledScorecardPreferenceTimezoneLengthRule(
    var maxLength: Int,
) : ScheduledScorecardPreferenceRule {
    override fun check(item: ScheduledScorecardPreference): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled scorecard preference. */
data class ScheduledScorecardPreferenceValidator(var rules: MutableList<ScheduledScorecardPreferenceRule>) {
    fun validate(item: ScheduledScorecardPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledScorecardPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledScorecardPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledScorecardPreferenceValidator(): ScheduledScorecardPreferenceValidator {
    return ScheduledScorecardPreferenceValidator(
        mutableListOf<ScheduledScorecardPreferenceRule>(
            ScheduledScorecardPreferenceAllowedExternalRefRule(mutableListOf<String>()),
            ScheduledScorecardPreferenceLabelLengthRule(120),
            ScheduledScorecardPreferenceRatioRangeRule(3.2, 2923.9),
            ScheduledScorecardPreferenceTimezoneLengthRule(255),
        ),
    )
}
