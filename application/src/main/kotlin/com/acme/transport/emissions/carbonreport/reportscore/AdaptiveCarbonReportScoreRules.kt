package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.text.normalize

/** A rule a adaptive carbon report score must pass. Check returns its problems, or none. */
interface AdaptiveCarbonReportScoreRule {
    fun check(item: AdaptiveCarbonReportScore): MutableList<String>
}

data class AdaptiveCarbonReportScoreDescriptionLengthRule(var maxLength: Int) : AdaptiveCarbonReportScoreRule {
    override fun check(item: AdaptiveCarbonReportScore): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportScoreNameLengthRule(var maxLength: Int) : AdaptiveCarbonReportScoreRule {
    override fun check(item: AdaptiveCarbonReportScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportScoreAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveCarbonReportScoreRule {
    override fun check(item: AdaptiveCarbonReportScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportScoreBudgetRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCarbonReportScoreRule {
    override fun check(item: AdaptiveCarbonReportScore): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportScoreTitleLengthRule(var maxLength: Int) : AdaptiveCarbonReportScoreRule {
    override fun check(item: AdaptiveCarbonReportScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive carbon report score. */
data class AdaptiveCarbonReportScoreValidator(var rules: MutableList<AdaptiveCarbonReportScoreRule>) {
    fun validate(item: AdaptiveCarbonReportScore): MutableList<String> {
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

    fun requireValid(item: AdaptiveCarbonReportScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCarbonReportScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCarbonReportScoreValidator(): AdaptiveCarbonReportScoreValidator {
    return AdaptiveCarbonReportScoreValidator(
        mutableListOf<AdaptiveCarbonReportScoreRule>(
            AdaptiveCarbonReportScoreDescriptionLengthRule(64),
            AdaptiveCarbonReportScoreNameLengthRule(32),
            AdaptiveCarbonReportScoreAllowedCodeRule(mutableListOf<String>("primary", "weighted")),
            AdaptiveCarbonReportScoreBudgetRangeRule(6.6, 2413.2),
            AdaptiveCarbonReportScoreTitleLengthRule(1000),
        ),
    )
}
