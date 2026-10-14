package com.acme.transport.emissions.carbonreport.strictreportadjustment

/** A rule a global carbon report policy must pass. Check returns its problems, or none. */
interface GlobalCarbonReportPolicyRule {
    fun check(item: GlobalCarbonReportPolicy): MutableList<String>
}

data class GlobalCarbonReportPolicyRequiredCodeRule(var message: String) : GlobalCarbonReportPolicyRule {
    override fun check(item: GlobalCarbonReportPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCarbonReportPolicyRequiredCurrencyRule(var message: String) : GlobalCarbonReportPolicyRule {
    override fun check(item: GlobalCarbonReportPolicy): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCarbonReportPolicyPriorityRangeRule(
    var min: Int,
    var max: Int,
) : GlobalCarbonReportPolicyRule {
    override fun check(item: GlobalCarbonReportPolicy): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class GlobalCarbonReportPolicyMarginRangeRule(
    var min: Double,
    var max: Double,
) : GlobalCarbonReportPolicyRule {
    override fun check(item: GlobalCarbonReportPolicy): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global carbon report policy. */
data class GlobalCarbonReportPolicyValidator(var rules: MutableList<GlobalCarbonReportPolicyRule>) {
    fun validate(item: GlobalCarbonReportPolicy): MutableList<String> {
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

    fun requireValid(item: GlobalCarbonReportPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCarbonReportPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCarbonReportPolicyValidator(): GlobalCarbonReportPolicyValidator {
    return GlobalCarbonReportPolicyValidator(
        mutableListOf<GlobalCarbonReportPolicyRule>(
            GlobalCarbonReportPolicyRequiredCodeRule("global carbon report policy needs this field"),
            GlobalCarbonReportPolicyRequiredCurrencyRule("global carbon report policy needs an entry"),
            GlobalCarbonReportPolicyPriorityRangeRule(32, 255),
            GlobalCarbonReportPolicyMarginRangeRule(5.1, 3223.6),
        ),
    )
}
