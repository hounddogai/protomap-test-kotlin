package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.text.normalize

/** A rule a default hub summary must pass. Check returns its problems, or none. */
interface DefaultHubSummaryRule {
    fun check(item: DefaultHubSummary): MutableList<String>
}

data class DefaultHubSummaryOdometerRangeRule(var min: Int, var max: Int) : DefaultHubSummaryRule {
    override fun check(item: DefaultHubSummary): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class DefaultHubSummaryCodeLengthRule(var maxLength: Int) : DefaultHubSummaryRule {
    override fun check(item: DefaultHubSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubSummaryAllowedCodeRule(var allowed: MutableList<String>) : DefaultHubSummaryRule {
    override fun check(item: DefaultHubSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubSummaryRequiredNameRule(var message: String) : DefaultHubSummaryRule {
    override fun check(item: DefaultHubSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubSummaryRequiredCategoryRule(var message: String) : DefaultHubSummaryRule {
    override fun check(item: DefaultHubSummary): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default hub summary. */
data class DefaultHubSummaryValidator(var rules: MutableList<DefaultHubSummaryRule>) {
    fun validate(item: DefaultHubSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultHubSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultHubSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultHubSummaryValidator(): DefaultHubSummaryValidator {
    return DefaultHubSummaryValidator(
        mutableListOf<DefaultHubSummaryRule>(
            DefaultHubSummaryOdometerRangeRule(16, 80),
            DefaultHubSummaryCodeLengthRule(64),
            DefaultHubSummaryAllowedCodeRule(mutableListOf<String>()),
            DefaultHubSummaryRequiredNameRule("default hub summary needs a value"),
            DefaultHubSummaryRequiredCategoryRule("default hub summary needs an entry"),
        ),
    )
}
