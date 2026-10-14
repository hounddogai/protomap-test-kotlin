package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.text.normalize

/** A rule a regional carbon report rule must pass. Check returns its problems, or none. */
interface RegionalCarbonReportRuleRule {
    fun check(item: RegionalCarbonReportRule): MutableList<String>
}

data class RegionalCarbonReportRuleLabelLengthRule(var maxLength: Int) : RegionalCarbonReportRuleRule {
    override fun check(item: RegionalCarbonReportRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportRuleCodePrefixRule(var prefix: String) : RegionalCarbonReportRuleRule {
    override fun check(item: RegionalCarbonReportRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportRuleRequiredContactEmailRule(
    var message: String,
) : RegionalCarbonReportRuleRule {
    override fun check(item: RegionalCarbonReportRule): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportRuleCodeLengthRule(var maxLength: Int) : RegionalCarbonReportRuleRule {
    override fun check(item: RegionalCarbonReportRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportRuleRequiredLabelRule(var message: String) : RegionalCarbonReportRuleRule {
    override fun check(item: RegionalCarbonReportRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportRuleAllowedLabelRule(
    var allowed: MutableList<String>,
) : RegionalCarbonReportRuleRule {
    override fun check(item: RegionalCarbonReportRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional carbon report rule. */
data class RegionalCarbonReportRuleValidator(var rules: MutableList<RegionalCarbonReportRuleRule>) {
    fun validate(item: RegionalCarbonReportRule): MutableList<String> {
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

    fun requireValid(item: RegionalCarbonReportRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCarbonReportRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCarbonReportRuleValidator(): RegionalCarbonReportRuleValidator {
    return RegionalCarbonReportRuleValidator(
        mutableListOf<RegionalCarbonReportRuleRule>(
            RegionalCarbonReportRuleLabelLengthRule(3600),
            RegionalCarbonReportRuleCodePrefixRule("ACM"),
            RegionalCarbonReportRuleRequiredContactEmailRule("regional carbon report rule needs this field"),
            RegionalCarbonReportRuleCodeLengthRule(3600),
            RegionalCarbonReportRuleRequiredLabelRule("regional carbon report rule needs an entry"),
            RegionalCarbonReportRuleAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
