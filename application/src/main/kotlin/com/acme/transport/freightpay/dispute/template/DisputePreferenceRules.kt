package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.text.normalize

/** A rule a dispute preference must pass. Check returns its problems, or none. */
interface DisputePreferenceRule {
    fun check(item: DisputePreference): MutableList<String>
}

data class DisputePreferenceRequiredSummaryRule(var message: String) : DisputePreferenceRule {
    override fun check(item: DisputePreference): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputePreferenceCodeLengthRule(var maxLength: Int) : DisputePreferenceRule {
    override fun check(item: DisputePreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputePreferenceAllowedSummaryRule(var allowed: MutableList<String>) : DisputePreferenceRule {
    override fun check(item: DisputePreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DisputePreferenceUpdatedAtRangeRule(var min: Int, var max: Int) : DisputePreferenceRule {
    override fun check(item: DisputePreference): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputePreferenceRequiredTitleRule(var message: String) : DisputePreferenceRule {
    override fun check(item: DisputePreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputePreferenceCodePrefixRule(var prefix: String) : DisputePreferenceRule {
    override fun check(item: DisputePreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute preference. */
data class DisputePreferenceValidator(var rules: MutableList<DisputePreferenceRule>) {
    fun validate(item: DisputePreference): MutableList<String> {
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

    fun requireValid(item: DisputePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputePreferenceValidator(): DisputePreferenceValidator {
    return DisputePreferenceValidator(
        mutableListOf<DisputePreferenceRule>(
            DisputePreferenceRequiredSummaryRule("dispute preference needs an entry"),
            DisputePreferenceCodeLengthRule(64),
            DisputePreferenceAllowedSummaryRule(mutableListOf<String>("adaptive", "manual", "adaptive")),
            DisputePreferenceUpdatedAtRangeRule(32, 255),
            DisputePreferenceRequiredTitleRule("dispute preference needs an entry"),
            DisputePreferenceCodePrefixRule("ACM"),
        ),
    )
}
