package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.text.normalize

/** A rule a default drop preference must pass. Check returns its problems, or none. */
interface DefaultDropPreferenceRule {
    fun check(item: DefaultDropPreference): MutableList<String>
}

data class DefaultDropPreferenceRequiredNameRule(var message: String) : DefaultDropPreferenceRule {
    override fun check(item: DefaultDropPreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropPreferenceSummaryLengthRule(var maxLength: Int) : DefaultDropPreferenceRule {
    override fun check(item: DefaultDropPreference): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropPreferenceNameLengthRule(var maxLength: Int) : DefaultDropPreferenceRule {
    override fun check(item: DefaultDropPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropPreferenceStopCountRangeRule(var min: Int, var max: Int) : DefaultDropPreferenceRule {
    override fun check(item: DefaultDropPreference): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropPreferenceAllowedCodeRule(var allowed: MutableList<String>) : DefaultDropPreferenceRule {
    override fun check(item: DefaultDropPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default drop preference. */
data class DefaultDropPreferenceValidator(var rules: MutableList<DefaultDropPreferenceRule>) {
    fun validate(item: DefaultDropPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDropPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDropPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDropPreferenceValidator(): DefaultDropPreferenceValidator {
    return DefaultDropPreferenceValidator(
        mutableListOf<DefaultDropPreferenceRule>(
            DefaultDropPreferenceRequiredNameRule("default drop preference needs this field"),
            DefaultDropPreferenceSummaryLengthRule(32),
            DefaultDropPreferenceNameLengthRule(80),
            DefaultDropPreferenceStopCountRangeRule(64, 86400),
            DefaultDropPreferenceAllowedCodeRule(mutableListOf<String>("pending", "capped")),
        ),
    )
}
