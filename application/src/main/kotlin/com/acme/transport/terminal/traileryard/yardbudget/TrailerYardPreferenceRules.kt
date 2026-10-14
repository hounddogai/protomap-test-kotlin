package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard preference must pass. Check returns its problems, or none. */
interface TrailerYardPreferenceRule {
    fun check(item: TrailerYardPreference): MutableList<String>
}

data class TrailerYardPreferenceAllowedLabelRule(var allowed: MutableList<String>) : TrailerYardPreferenceRule {
    override fun check(item: TrailerYardPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPreferenceExpiresAtRangeRule(var min: Int, var max: Int) : TrailerYardPreferenceRule {
    override fun check(item: TrailerYardPreference): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPreferenceBatchSizeRangeRule(var min: Int, var max: Int) : TrailerYardPreferenceRule {
    override fun check(item: TrailerYardPreference): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPreferenceRequiredDescriptionRule(var message: String) : TrailerYardPreferenceRule {
    override fun check(item: TrailerYardPreference): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPreferenceDescriptionLengthRule(var maxLength: Int) : TrailerYardPreferenceRule {
    override fun check(item: TrailerYardPreference): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPreferenceCodePrefixRule(var prefix: String) : TrailerYardPreferenceRule {
    override fun check(item: TrailerYardPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard preference. */
data class TrailerYardPreferenceValidator(var rules: MutableList<TrailerYardPreferenceRule>) {
    fun validate(item: TrailerYardPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardPreferenceValidator(): TrailerYardPreferenceValidator {
    return TrailerYardPreferenceValidator(
        mutableListOf<TrailerYardPreferenceRule>(
            TrailerYardPreferenceAllowedLabelRule(mutableListOf<String>("capped")),
            TrailerYardPreferenceExpiresAtRangeRule(80, 80),
            TrailerYardPreferenceBatchSizeRangeRule(120, 255),
            TrailerYardPreferenceRequiredDescriptionRule("trailer yard preference needs an entry"),
            TrailerYardPreferenceDescriptionLengthRule(80),
            TrailerYardPreferenceCodePrefixRule("OPS"),
        ),
    )
}
