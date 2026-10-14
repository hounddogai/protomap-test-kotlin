package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.text.normalize

/** A rule a manual carbon report rule must pass. Check returns its problems, or none. */
interface ManualCarbonReportRuleRule {
    fun check(item: ManualCarbonReportRule): MutableList<String>
}

data class ManualCarbonReportRuleDiscountRangeRule(
    var min: Double,
    var max: Double,
) : ManualCarbonReportRuleRule {
    override fun check(item: ManualCarbonReportRule): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualCarbonReportRuleRequiredCodeRule(var message: String) : ManualCarbonReportRuleRule {
    override fun check(item: ManualCarbonReportRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCarbonReportRuleMaximumRangeRule(
    var min: Double,
    var max: Double,
) : ManualCarbonReportRuleRule {
    override fun check(item: ManualCarbonReportRule): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualCarbonReportRuleRequiredPhoneRule(var message: String) : ManualCarbonReportRuleRule {
    override fun check(item: ManualCarbonReportRule): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCarbonReportRuleAllowedLabelRule(
    var allowed: MutableList<String>,
) : ManualCarbonReportRuleRule {
    override fun check(item: ManualCarbonReportRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual carbon report rule. */
data class ManualCarbonReportRuleValidator(var rules: MutableList<ManualCarbonReportRuleRule>) {
    fun validate(item: ManualCarbonReportRule): MutableList<String> {
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

    fun requireValid(item: ManualCarbonReportRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCarbonReportRule>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCarbonReportRuleValidator(): ManualCarbonReportRuleValidator {
    return ManualCarbonReportRuleValidator(
        mutableListOf<ManualCarbonReportRuleRule>(
            ManualCarbonReportRuleDiscountRangeRule(9.7, 3901.2),
            ManualCarbonReportRuleRequiredCodeRule("manual carbon report rule needs a value"),
            ManualCarbonReportRuleMaximumRangeRule(8.3, 2883.8),
            ManualCarbonReportRuleRequiredPhoneRule("manual carbon report rule needs a value"),
            ManualCarbonReportRuleAllowedLabelRule(mutableListOf<String>("capped", "rolling", "global")),
        ),
    )
}
