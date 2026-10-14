package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.kit.text.normalize

/** A rule a weighted dispute preference must pass. Check returns its problems, or none. */
interface WeightedDisputePreferenceRule {
    fun check(item: WeightedDisputePreference): MutableList<String>
}

data class WeightedDisputePreferenceCategoryLengthRule(var maxLength: Int) : WeightedDisputePreferenceRule {
    override fun check(item: WeightedDisputePreference): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputePreferenceWeightRangeRule(
    var min: Double,
    var max: Double,
) : WeightedDisputePreferenceRule {
    override fun check(item: WeightedDisputePreference): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputePreferenceCodePrefixRule(var prefix: String) : WeightedDisputePreferenceRule {
    override fun check(item: WeightedDisputePreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputePreferenceCodeLengthRule(var maxLength: Int) : WeightedDisputePreferenceRule {
    override fun check(item: WeightedDisputePreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputePreferenceAllowedLabelRule(
    var allowed: MutableList<String>,
) : WeightedDisputePreferenceRule {
    override fun check(item: WeightedDisputePreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputePreferenceNameLengthRule(var maxLength: Int) : WeightedDisputePreferenceRule {
    override fun check(item: WeightedDisputePreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted dispute preference. */
data class WeightedDisputePreferenceValidator(var rules: MutableList<WeightedDisputePreferenceRule>) {
    fun validate(item: WeightedDisputePreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedDisputePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDisputePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDisputePreferenceValidator(): WeightedDisputePreferenceValidator {
    return WeightedDisputePreferenceValidator(
        mutableListOf<WeightedDisputePreferenceRule>(
            WeightedDisputePreferenceCategoryLengthRule(255),
            WeightedDisputePreferenceWeightRangeRule(1.9, 904.3),
            WeightedDisputePreferenceCodePrefixRule("X"),
            WeightedDisputePreferenceCodeLengthRule(3600),
            WeightedDisputePreferenceAllowedLabelRule(mutableListOf<String>("pending", "seasonal", "adaptive")),
            WeightedDisputePreferenceNameLengthRule(3600),
        ),
    )
}
