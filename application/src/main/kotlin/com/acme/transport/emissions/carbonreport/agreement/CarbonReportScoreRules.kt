package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.kit.text.normalize

/** A rule a carbon report score must pass. Check returns its problems, or none. */
interface CarbonReportScoreRule {
    fun check(item: CarbonReportScore): MutableList<String>
}

data class CarbonReportScoreAllowedCodeRule(var allowed: MutableList<String>) : CarbonReportScoreRule {
    override fun check(item: CarbonReportScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportScoreRequiredTitleRule(var message: String) : CarbonReportScoreRule {
    override fun check(item: CarbonReportScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportScoreLoadKgRangeRule(var min: Double, var max: Double) : CarbonReportScoreRule {
    override fun check(item: CarbonReportScore): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report score. */
data class CarbonReportScoreValidator(var rules: MutableList<CarbonReportScoreRule>) {
    fun validate(item: CarbonReportScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportScore>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportScoreValidator(): CarbonReportScoreValidator {
    return CarbonReportScoreValidator(
        mutableListOf<CarbonReportScoreRule>(
            CarbonReportScoreAllowedCodeRule(mutableListOf<String>()),
            CarbonReportScoreRequiredTitleRule("carbon report score needs this field"),
            CarbonReportScoreLoadKgRangeRule(7.1, 4126.0),
        ),
    )
}
