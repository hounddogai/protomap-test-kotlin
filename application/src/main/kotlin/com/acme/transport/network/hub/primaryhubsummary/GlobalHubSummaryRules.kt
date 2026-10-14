package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.text.normalize

/** A rule a global hub summary must pass. Check returns its problems, or none. */
interface GlobalHubSummaryRule {
    fun check(item: GlobalHubSummary): MutableList<String>
}

data class GlobalHubSummaryAllowedCodeRule(var allowed: MutableList<String>) : GlobalHubSummaryRule {
    override fun check(item: GlobalHubSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubSummaryWeightRangeRule(var min: Double, var max: Double) : GlobalHubSummaryRule {
    override fun check(item: GlobalHubSummary): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class GlobalHubSummaryThresholdRangeRule(var min: Double, var max: Double) : GlobalHubSummaryRule {
    override fun check(item: GlobalHubSummary): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class GlobalHubSummaryPalletsRangeRule(var min: Int, var max: Int) : GlobalHubSummaryRule {
    override fun check(item: GlobalHubSummary): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubSummaryRequiredNameRule(var message: String) : GlobalHubSummaryRule {
    override fun check(item: GlobalHubSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubSummaryCodePrefixRule(var prefix: String) : GlobalHubSummaryRule {
    override fun check(item: GlobalHubSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global hub summary. */
data class GlobalHubSummaryValidator(var rules: MutableList<GlobalHubSummaryRule>) {
    fun validate(item: GlobalHubSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalHubSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalHubSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalHubSummaryValidator(): GlobalHubSummaryValidator {
    return GlobalHubSummaryValidator(
        mutableListOf<GlobalHubSummaryRule>(
            GlobalHubSummaryAllowedCodeRule(mutableListOf<String>("regional", "legacy")),
            GlobalHubSummaryWeightRangeRule(2.9, 2544.6),
            GlobalHubSummaryThresholdRangeRule(1.9, 1326.4),
            GlobalHubSummaryPalletsRangeRule(255, 1000),
            GlobalHubSummaryRequiredNameRule("global hub summary needs this field"),
            GlobalHubSummaryCodePrefixRule("OPS"),
        ),
    )
}
