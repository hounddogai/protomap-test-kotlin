package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.text.normalize

/** A rule a carbon report override must pass. Check returns its problems, or none. */
interface CarbonReportOverrideRule {
    fun check(item: CarbonReportOverride): MutableList<String>
}

data class CarbonReportOverrideRequiredCodeRule(var message: String) : CarbonReportOverrideRule {
    override fun check(item: CarbonReportOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportOverrideRequiredNameRule(var message: String) : CarbonReportOverrideRule {
    override fun check(item: CarbonReportOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportOverrideAllowedReviewerRule(
    var allowed: MutableList<String>,
) : CarbonReportOverrideRule {
    override fun check(item: CarbonReportOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportOverrideRequiredReviewerRule(var message: String) : CarbonReportOverrideRule {
    override fun check(item: CarbonReportOverride): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report override. */
data class CarbonReportOverrideValidator(var rules: MutableList<CarbonReportOverrideRule>) {
    fun validate(item: CarbonReportOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportOverrideValidator(): CarbonReportOverrideValidator {
    return CarbonReportOverrideValidator(
        mutableListOf<CarbonReportOverrideRule>(
            CarbonReportOverrideRequiredCodeRule("carbon report override needs an entry"),
            CarbonReportOverrideRequiredNameRule("carbon report override needs this field"),
            CarbonReportOverrideAllowedReviewerRule(mutableListOf<String>()),
            CarbonReportOverrideRequiredReviewerRule("carbon report override needs an entry"),
        ),
    )
}
