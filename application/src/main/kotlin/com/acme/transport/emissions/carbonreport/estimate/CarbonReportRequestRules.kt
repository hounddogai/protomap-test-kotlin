package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.text.normalize

/** A rule a carbon report request must pass. Check returns its problems, or none. */
interface CarbonReportRequestRule {
    fun check(item: CarbonReportRequest): MutableList<String>
}

data class CarbonReportRequestCodeLengthRule(var maxLength: Int) : CarbonReportRequestRule {
    override fun check(item: CarbonReportRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportRequestTitleLengthRule(var maxLength: Int) : CarbonReportRequestRule {
    override fun check(item: CarbonReportRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportRequestAllowedTitleRule(var allowed: MutableList<String>) : CarbonReportRequestRule {
    override fun check(item: CarbonReportRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report request. */
data class CarbonReportRequestValidator(var rules: MutableList<CarbonReportRequestRule>) {
    fun validate(item: CarbonReportRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportRequestValidator(): CarbonReportRequestValidator {
    return CarbonReportRequestValidator(
        mutableListOf<CarbonReportRequestRule>(
            CarbonReportRequestCodeLengthRule(32),
            CarbonReportRequestTitleLengthRule(32),
            CarbonReportRequestAllowedTitleRule(mutableListOf<String>("regional")),
        ),
    )
}
