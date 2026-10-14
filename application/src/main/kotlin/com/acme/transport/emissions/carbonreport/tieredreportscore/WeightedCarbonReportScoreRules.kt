package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.text.normalize

/** A rule a weighted carbon report score must pass. Check returns its problems, or none. */
interface WeightedCarbonReportScoreRule {
    fun check(item: WeightedCarbonReportScore): MutableList<String>
}

data class WeightedCarbonReportScoreCodePrefixRule(var prefix: String) : WeightedCarbonReportScoreRule {
    override fun check(item: WeightedCarbonReportScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportScoreShiftCodeLengthRule(var maxLength: Int) : WeightedCarbonReportScoreRule {
    override fun check(item: WeightedCarbonReportScore): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportScoreExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedCarbonReportScoreRule {
    override fun check(item: WeightedCarbonReportScore): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class WeightedCarbonReportScoreAllowedLabelRule(
    var allowed: MutableList<String>,
) : WeightedCarbonReportScoreRule {
    override fun check(item: WeightedCarbonReportScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportScoreRequiredCodeRule(var message: String) : WeightedCarbonReportScoreRule {
    override fun check(item: WeightedCarbonReportScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportScoreStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedCarbonReportScoreRule {
    override fun check(item: WeightedCarbonReportScore): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted carbon report score. */
data class WeightedCarbonReportScoreValidator(var rules: MutableList<WeightedCarbonReportScoreRule>) {
    fun validate(item: WeightedCarbonReportScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCarbonReportScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCarbonReportScore>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCarbonReportScoreValidator(): WeightedCarbonReportScoreValidator {
    return WeightedCarbonReportScoreValidator(
        mutableListOf<WeightedCarbonReportScoreRule>(
            WeightedCarbonReportScoreCodePrefixRule("ACM"),
            WeightedCarbonReportScoreShiftCodeLengthRule(1000),
            WeightedCarbonReportScoreExpiresAtRangeRule(120, 120),
            WeightedCarbonReportScoreAllowedLabelRule(mutableListOf<String>()),
            WeightedCarbonReportScoreRequiredCodeRule("weighted carbon report score needs a value"),
            WeightedCarbonReportScoreStartsAtRangeRule(32, 3600),
        ),
    )
}
