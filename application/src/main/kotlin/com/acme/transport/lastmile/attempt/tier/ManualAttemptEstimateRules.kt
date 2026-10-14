package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.text.normalize

/** A rule a manual attempt estimate must pass. Check returns its problems, or none. */
interface ManualAttemptEstimateRule {
    fun check(item: ManualAttemptEstimate): MutableList<String>
}

data class ManualAttemptEstimateRequiredLabelRule(var message: String) : ManualAttemptEstimateRule {
    override fun check(item: ManualAttemptEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptEstimateAllowedLabelRule(var allowed: MutableList<String>) : ManualAttemptEstimateRule {
    override fun check(item: ManualAttemptEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptEstimateCodePrefixRule(var prefix: String) : ManualAttemptEstimateRule {
    override fun check(item: ManualAttemptEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual attempt estimate. */
data class ManualAttemptEstimateValidator(var rules: MutableList<ManualAttemptEstimateRule>) {
    fun validate(item: ManualAttemptEstimate): MutableList<String> {
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

    fun requireValid(item: ManualAttemptEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAttemptEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAttemptEstimateValidator(): ManualAttemptEstimateValidator {
    return ManualAttemptEstimateValidator(
        mutableListOf<ManualAttemptEstimateRule>(
            ManualAttemptEstimateRequiredLabelRule("manual attempt estimate needs an entry"),
            ManualAttemptEstimateAllowedLabelRule(mutableListOf<String>("tiered")),
            ManualAttemptEstimateCodePrefixRule("X"),
        ),
    )
}
