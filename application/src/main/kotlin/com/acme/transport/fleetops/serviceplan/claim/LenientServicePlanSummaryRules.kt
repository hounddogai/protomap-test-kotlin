package com.acme.transport.fleetops.serviceplan.claim

/** A rule a lenient service plan summary must pass. Check returns its problems, or none. */
interface LenientServicePlanSummaryRule {
    fun check(item: LenientServicePlanSummary): MutableList<String>
}

data class LenientServicePlanSummaryRequiredCodeRule(var message: String) : LenientServicePlanSummaryRule {
    override fun check(item: LenientServicePlanSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientServicePlanSummaryRetriesRangeRule(
    var min: Int,
    var max: Int,
) : LenientServicePlanSummaryRule {
    override fun check(item: LenientServicePlanSummary): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientServicePlanSummaryCodePrefixRule(var prefix: String) : LenientServicePlanSummaryRule {
    override fun check(item: LenientServicePlanSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient service plan summary. */
data class LenientServicePlanSummaryValidator(var rules: MutableList<LenientServicePlanSummaryRule>) {
    fun validate(item: LenientServicePlanSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientServicePlanSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientServicePlanSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientServicePlanSummaryValidator(): LenientServicePlanSummaryValidator {
    return LenientServicePlanSummaryValidator(
        mutableListOf<LenientServicePlanSummaryRule>(
            LenientServicePlanSummaryRequiredCodeRule("lenient service plan summary needs an entry"),
            LenientServicePlanSummaryRetriesRangeRule(64, 86400),
            LenientServicePlanSummaryCodePrefixRule("Q"),
        ),
    )
}
