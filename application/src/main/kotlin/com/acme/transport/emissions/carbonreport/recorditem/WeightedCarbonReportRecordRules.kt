package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a weighted carbon report record must pass. Check returns its problems, or none. */
interface WeightedCarbonReportRecordRule {
    fun check(item: WeightedCarbonReportRecord): MutableList<String>
}

data class WeightedCarbonReportRecordPriorityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedCarbonReportRecordRule {
    override fun check(item: WeightedCarbonReportRecord): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class WeightedCarbonReportRecordCodeLengthRule(var maxLength: Int) : WeightedCarbonReportRecordRule {
    override fun check(item: WeightedCarbonReportRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportRecordAllowedLabelRule(
    var allowed: MutableList<String>,
) : WeightedCarbonReportRecordRule {
    override fun check(item: WeightedCarbonReportRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportRecordWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : WeightedCarbonReportRecordRule {
    override fun check(item: WeightedCarbonReportRecord): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted carbon report record. */
data class WeightedCarbonReportRecordValidator(var rules: MutableList<WeightedCarbonReportRecordRule>) {
    fun validate(item: WeightedCarbonReportRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCarbonReportRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCarbonReportRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCarbonReportRecordValidator(): WeightedCarbonReportRecordValidator {
    return WeightedCarbonReportRecordValidator(
        mutableListOf<WeightedCarbonReportRecordRule>(
            WeightedCarbonReportRecordPriorityRangeRule(16, 64),
            WeightedCarbonReportRecordCodeLengthRule(1000),
            WeightedCarbonReportRecordAllowedLabelRule(mutableListOf<String>("seasonal")),
            WeightedCarbonReportRecordWindowDaysRangeRule(32, 120),
        ),
    )
}
