package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.text.normalize

/** A rule a default hub estimate must pass. Check returns its problems, or none. */
interface DefaultHubEstimateRule {
    fun check(item: DefaultHubEstimate): MutableList<String>
}

data class DefaultHubEstimateLabelLengthRule(var maxLength: Int) : DefaultHubEstimateRule {
    override fun check(item: DefaultHubEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubEstimateCountryLengthRule(var maxLength: Int) : DefaultHubEstimateRule {
    override fun check(item: DefaultHubEstimate): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubEstimateRequiredLabelRule(var message: String) : DefaultHubEstimateRule {
    override fun check(item: DefaultHubEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubEstimateRequiredCodeRule(var message: String) : DefaultHubEstimateRule {
    override fun check(item: DefaultHubEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubEstimateAllowedLabelRule(var allowed: MutableList<String>) : DefaultHubEstimateRule {
    override fun check(item: DefaultHubEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default hub estimate. */
data class DefaultHubEstimateValidator(var rules: MutableList<DefaultHubEstimateRule>) {
    fun validate(item: DefaultHubEstimate): MutableList<String> {
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

    fun requireValid(item: DefaultHubEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultHubEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultHubEstimateValidator(): DefaultHubEstimateValidator {
    return DefaultHubEstimateValidator(
        mutableListOf<DefaultHubEstimateRule>(
            DefaultHubEstimateLabelLengthRule(255),
            DefaultHubEstimateCountryLengthRule(16),
            DefaultHubEstimateRequiredLabelRule("default hub estimate needs an entry"),
            DefaultHubEstimateRequiredCodeRule("default hub estimate needs this field"),
            DefaultHubEstimateAllowedLabelRule(mutableListOf<String>("seasonal", "pending", "global")),
        ),
    )
}
