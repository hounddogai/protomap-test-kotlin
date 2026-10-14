package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.kit.text.normalize

/** A rule a regional carbon report request must pass. Check returns its problems, or none. */
interface RegionalCarbonReportRequestRule {
    fun check(item: RegionalCarbonReportRequest): MutableList<String>
}

data class RegionalCarbonReportRequestAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalCarbonReportRequestRule {
    override fun check(item: RegionalCarbonReportRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportRequestRequiredTitleRule(var message: String) : RegionalCarbonReportRequestRule {
    override fun check(item: RegionalCarbonReportRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportRequestStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCarbonReportRequestRule {
    override fun check(item: RegionalCarbonReportRequest): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional carbon report request. */
data class RegionalCarbonReportRequestValidator(var rules: MutableList<RegionalCarbonReportRequestRule>) {
    fun validate(item: RegionalCarbonReportRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCarbonReportRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCarbonReportRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCarbonReportRequestValidator(): RegionalCarbonReportRequestValidator {
    return RegionalCarbonReportRequestValidator(
        mutableListOf<RegionalCarbonReportRequestRule>(
            RegionalCarbonReportRequestAllowedCodeRule(mutableListOf<String>("weighted")),
            RegionalCarbonReportRequestRequiredTitleRule("regional carbon report request needs this field"),
            RegionalCarbonReportRequestStartsAtRangeRule(255, 3600),
        ),
    )
}
