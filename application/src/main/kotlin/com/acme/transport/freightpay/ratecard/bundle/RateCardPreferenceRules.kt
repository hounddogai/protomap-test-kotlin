package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.text.normalize

/** A rule a rate card preference must pass. Check returns its problems, or none. */
interface RateCardPreferenceRule {
    fun check(item: RateCardPreference): MutableList<String>
}

data class RateCardPreferenceRequiredCodeRule(var message: String) : RateCardPreferenceRule {
    override fun check(item: RateCardPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardPreferenceRequiredLabelRule(var message: String) : RateCardPreferenceRule {
    override fun check(item: RateCardPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardPreferenceAllowedLabelRule(var allowed: MutableList<String>) : RateCardPreferenceRule {
    override fun check(item: RateCardPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RateCardPreferenceRequiredLaneRule(var message: String) : RateCardPreferenceRule {
    override fun check(item: RateCardPreference): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardPreferenceCodeLengthRule(var maxLength: Int) : RateCardPreferenceRule {
    override fun check(item: RateCardPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardPreferenceLaneLengthRule(var maxLength: Int) : RateCardPreferenceRule {
    override fun check(item: RateCardPreference): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card preference. */
data class RateCardPreferenceValidator(var rules: MutableList<RateCardPreferenceRule>) {
    fun validate(item: RateCardPreference): MutableList<String> {
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

    fun requireValid(item: RateCardPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardPreferenceValidator(): RateCardPreferenceValidator {
    return RateCardPreferenceValidator(
        mutableListOf<RateCardPreferenceRule>(
            RateCardPreferenceRequiredCodeRule("rate card preference needs an entry"),
            RateCardPreferenceRequiredLabelRule("rate card preference needs a value"),
            RateCardPreferenceAllowedLabelRule(mutableListOf<String>()),
            RateCardPreferenceRequiredLaneRule("rate card preference needs a value"),
            RateCardPreferenceCodeLengthRule(3600),
            RateCardPreferenceLaneLengthRule(80),
        ),
    )
}
