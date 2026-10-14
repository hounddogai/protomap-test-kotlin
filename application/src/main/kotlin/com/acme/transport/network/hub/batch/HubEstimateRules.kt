package com.acme.transport.network.hub.batch

/** A rule a hub estimate must pass. Check returns its problems, or none. */
interface HubEstimateRule {
    fun check(item: HubEstimate): MutableList<String>
}

data class HubEstimateCurrencyLengthRule(var maxLength: Int) : HubEstimateRule {
    override fun check(item: HubEstimate): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubEstimateRequiredCurrencyRule(var message: String) : HubEstimateRule {
    override fun check(item: HubEstimate): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubEstimateRequiredCodeRule(var message: String) : HubEstimateRule {
    override fun check(item: HubEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub estimate. */
data class HubEstimateValidator(var rules: MutableList<HubEstimateRule>) {
    fun validate(item: HubEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultHubEstimateValidator(): HubEstimateValidator {
    return HubEstimateValidator(
        mutableListOf<HubEstimateRule>(
            HubEstimateCurrencyLengthRule(120),
            HubEstimateRequiredCurrencyRule("hub estimate needs this field"),
            HubEstimateRequiredCodeRule("hub estimate needs this field"),
        ),
    )
}
