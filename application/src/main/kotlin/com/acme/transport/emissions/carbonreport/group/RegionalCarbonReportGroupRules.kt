package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.text.normalize

/** A rule a regional carbon report group must pass. Check returns its problems, or none. */
interface RegionalCarbonReportGroupRule {
    fun check(item: RegionalCarbonReportGroup): MutableList<String>
}

data class RegionalCarbonReportGroupRequiredNameRule(var message: String) : RegionalCarbonReportGroupRule {
    override fun check(item: RegionalCarbonReportGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportGroupAllowedLabelRule(
    var allowed: MutableList<String>,
) : RegionalCarbonReportGroupRule {
    override fun check(item: RegionalCarbonReportGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportGroupCodePrefixRule(var prefix: String) : RegionalCarbonReportGroupRule {
    override fun check(item: RegionalCarbonReportGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportGroupAmountRangeRule(
    var min: Double,
    var max: Double,
) : RegionalCarbonReportGroupRule {
    override fun check(item: RegionalCarbonReportGroup): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportGroupCodeLengthRule(var maxLength: Int) : RegionalCarbonReportGroupRule {
    override fun check(item: RegionalCarbonReportGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportGroupRequiredLabelRule(var message: String) : RegionalCarbonReportGroupRule {
    override fun check(item: RegionalCarbonReportGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional carbon report group. */
data class RegionalCarbonReportGroupValidator(var rules: MutableList<RegionalCarbonReportGroupRule>) {
    fun validate(item: RegionalCarbonReportGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCarbonReportGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCarbonReportGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCarbonReportGroupValidator(): RegionalCarbonReportGroupValidator {
    return RegionalCarbonReportGroupValidator(
        mutableListOf<RegionalCarbonReportGroupRule>(
            RegionalCarbonReportGroupRequiredNameRule("regional carbon report group needs this field"),
            RegionalCarbonReportGroupAllowedLabelRule(mutableListOf<String>("regional")),
            RegionalCarbonReportGroupCodePrefixRule("ACM"),
            RegionalCarbonReportGroupAmountRangeRule(1.7, 1247.6),
            RegionalCarbonReportGroupCodeLengthRule(255),
            RegionalCarbonReportGroupRequiredLabelRule("regional carbon report group needs an entry"),
        ),
    )
}
