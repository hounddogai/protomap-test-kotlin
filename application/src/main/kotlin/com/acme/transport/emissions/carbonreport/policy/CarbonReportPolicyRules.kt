package com.acme.transport.emissions.carbonreport.policy

/** A rule a carbon report policy must pass. Check returns its problems, or none. */
interface CarbonReportPolicyRule {
    fun check(item: CarbonReportPolicy): MutableList<String>
}

data class CarbonReportPolicyRequiredDescriptionRule(var message: String) : CarbonReportPolicyRule {
    override fun check(item: CarbonReportPolicy): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportPolicyStopCountRangeRule(var min: Int, var max: Int) : CarbonReportPolicyRule {
    override fun check(item: CarbonReportPolicy): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class CarbonReportPolicyRequiredCurrencyRule(var message: String) : CarbonReportPolicyRule {
    override fun check(item: CarbonReportPolicy): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report policy. */
data class CarbonReportPolicyValidator(var rules: MutableList<CarbonReportPolicyRule>) {
    fun validate(item: CarbonReportPolicy): MutableList<String> {
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

    fun requireValid(item: CarbonReportPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportPolicyValidator(): CarbonReportPolicyValidator {
    return CarbonReportPolicyValidator(
        mutableListOf<CarbonReportPolicyRule>(
            CarbonReportPolicyRequiredDescriptionRule("carbon report policy needs this field"),
            CarbonReportPolicyStopCountRangeRule(16, 16),
            CarbonReportPolicyRequiredCurrencyRule("carbon report policy needs a value"),
        ),
    )
}
