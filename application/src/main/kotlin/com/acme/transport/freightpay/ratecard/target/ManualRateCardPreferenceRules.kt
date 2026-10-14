package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.text.normalize

/** A rule a manual rate card preference must pass. Check returns its problems, or none. */
interface ManualRateCardPreferenceRule {
    fun check(item: ManualRateCardPreference): MutableList<String>
}

data class ManualRateCardPreferenceCodeLengthRule(var maxLength: Int) : ManualRateCardPreferenceRule {
    override fun check(item: ManualRateCardPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardPreferenceLabelLengthRule(var maxLength: Int) : ManualRateCardPreferenceRule {
    override fun check(item: ManualRateCardPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardPreferenceAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : ManualRateCardPreferenceRule {
    override fun check(item: ManualRateCardPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual rate card preference. */
data class ManualRateCardPreferenceValidator(var rules: MutableList<ManualRateCardPreferenceRule>) {
    fun validate(item: ManualRateCardPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRateCardPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRateCardPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRateCardPreferenceValidator(): ManualRateCardPreferenceValidator {
    return ManualRateCardPreferenceValidator(
        mutableListOf<ManualRateCardPreferenceRule>(
            ManualRateCardPreferenceCodeLengthRule(3600),
            ManualRateCardPreferenceLabelLengthRule(255),
            ManualRateCardPreferenceAllowedTrailerIdRule(mutableListOf<String>("global")),
        ),
    )
}
