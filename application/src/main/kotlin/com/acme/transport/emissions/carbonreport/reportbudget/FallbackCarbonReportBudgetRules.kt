package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.text.normalize

/** A rule a fallback carbon report budget must pass. Check returns its problems, or none. */
interface FallbackCarbonReportBudgetRule {
    fun check(item: FallbackCarbonReportBudget): MutableList<String>
}

data class FallbackCarbonReportBudgetCodePrefixRule(var prefix: String) : FallbackCarbonReportBudgetRule {
    override fun check(item: FallbackCarbonReportBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportBudgetAllowedDepotRule(
    var allowed: MutableList<String>,
) : FallbackCarbonReportBudgetRule {
    override fun check(item: FallbackCarbonReportBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportBudgetRequiredCodeRule(var message: String) : FallbackCarbonReportBudgetRule {
    override fun check(item: FallbackCarbonReportBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportBudgetRequiredDepotRule(var message: String) : FallbackCarbonReportBudgetRule {
    override fun check(item: FallbackCarbonReportBudget): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportBudgetRetriesRangeRule(
    var min: Int,
    var max: Int,
) : FallbackCarbonReportBudgetRule {
    override fun check(item: FallbackCarbonReportBudget): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback carbon report budget. */
data class FallbackCarbonReportBudgetValidator(var rules: MutableList<FallbackCarbonReportBudgetRule>) {
    fun validate(item: FallbackCarbonReportBudget): MutableList<String> {
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

    fun requireValid(item: FallbackCarbonReportBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCarbonReportBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCarbonReportBudgetValidator(): FallbackCarbonReportBudgetValidator {
    return FallbackCarbonReportBudgetValidator(
        mutableListOf<FallbackCarbonReportBudgetRule>(
            FallbackCarbonReportBudgetCodePrefixRule("ACM"),
            FallbackCarbonReportBudgetAllowedDepotRule(mutableListOf<String>("adaptive")),
            FallbackCarbonReportBudgetRequiredCodeRule("fallback carbon report budget needs an entry"),
            FallbackCarbonReportBudgetRequiredDepotRule("fallback carbon report budget needs a value"),
            FallbackCarbonReportBudgetRetriesRangeRule(16, 3600),
        ),
    )
}
