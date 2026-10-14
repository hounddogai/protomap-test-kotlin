package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.text.normalize

/** A rule a carbon report group must pass. Check returns its problems, or none. */
interface CarbonReportGroupRule {
    fun check(item: CarbonReportGroup): MutableList<String>
}

data class CarbonReportGroupCodeLengthRule(var maxLength: Int) : CarbonReportGroupRule {
    override fun check(item: CarbonReportGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportGroupAllowedLabelRule(var allowed: MutableList<String>) : CarbonReportGroupRule {
    override fun check(item: CarbonReportGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportGroupCodePrefixRule(var prefix: String) : CarbonReportGroupRule {
    override fun check(item: CarbonReportGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportGroupWeightRangeRule(var min: Double, var max: Double) : CarbonReportGroupRule {
    override fun check(item: CarbonReportGroup): MutableList<String> {
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

data class CarbonReportGroupRequiredLabelRule(var message: String) : CarbonReportGroupRule {
    override fun check(item: CarbonReportGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportGroupMinimumRangeRule(var min: Double, var max: Double) : CarbonReportGroupRule {
    override fun check(item: CarbonReportGroup): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a carbon report group. */
data class CarbonReportGroupValidator(var rules: MutableList<CarbonReportGroupRule>) {
    fun validate(item: CarbonReportGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportGroupValidator(): CarbonReportGroupValidator {
    return CarbonReportGroupValidator(
        mutableListOf<CarbonReportGroupRule>(
            CarbonReportGroupCodeLengthRule(32),
            CarbonReportGroupAllowedLabelRule(mutableListOf<String>()),
            CarbonReportGroupCodePrefixRule("OPS"),
            CarbonReportGroupWeightRangeRule(7.2, 1691.7),
            CarbonReportGroupRequiredLabelRule("carbon report group needs a value"),
            CarbonReportGroupMinimumRangeRule(4.5, 1217.0),
        ),
    )
}
