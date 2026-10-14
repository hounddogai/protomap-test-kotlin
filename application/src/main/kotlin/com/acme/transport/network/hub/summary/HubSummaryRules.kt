package com.acme.transport.network.hub.summary

import com.acme.transport.kit.text.normalize

/** A rule a hub summary must pass. Check returns its problems, or none. */
interface HubSummaryRule {
    fun check(item: HubSummary): MutableList<String>
}

data class HubSummaryAllowedLabelRule(var allowed: MutableList<String>) : HubSummaryRule {
    override fun check(item: HubSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class HubSummaryLabelLengthRule(var maxLength: Int) : HubSummaryRule {
    override fun check(item: HubSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubSummaryOdometerRangeRule(var min: Int, var max: Int) : HubSummaryRule {
    override fun check(item: HubSummary): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubSummaryCodePrefixRule(var prefix: String) : HubSummaryRule {
    override fun check(item: HubSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub summary. */
data class HubSummaryValidator(var rules: MutableList<HubSummaryRule>) {
    fun validate(item: HubSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultHubSummaryValidator(): HubSummaryValidator {
    return HubSummaryValidator(
        mutableListOf<HubSummaryRule>(
            HubSummaryAllowedLabelRule(mutableListOf<String>("capped", "fallback", "weighted")),
            HubSummaryLabelLengthRule(1000),
            HubSummaryOdometerRangeRule(32, 255),
            HubSummaryCodePrefixRule("Q"),
        ),
    )
}
