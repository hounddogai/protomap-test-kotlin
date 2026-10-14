package com.acme.transport.emissions.carbonreport.reportestimate

/** A rule a weighted carbon report estimate must pass. Check returns its problems, or none. */
interface WeightedCarbonReportEstimateRule {
    fun check(item: WeightedCarbonReportEstimate): MutableList<String>
}

data class WeightedCarbonReportEstimateWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : WeightedCarbonReportEstimateRule {
    override fun check(item: WeightedCarbonReportEstimate): MutableList<String> {
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

data class WeightedCarbonReportEstimateRouteCodeLengthRule(
    var maxLength: Int,
) : WeightedCarbonReportEstimateRule {
    override fun check(item: WeightedCarbonReportEstimate): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportEstimateUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedCarbonReportEstimateRule {
    override fun check(item: WeightedCarbonReportEstimate): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted carbon report estimate. */
data class WeightedCarbonReportEstimateValidator(var rules: MutableList<WeightedCarbonReportEstimateRule>) {
    fun validate(item: WeightedCarbonReportEstimate): MutableList<String> {
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

    fun requireValid(item: WeightedCarbonReportEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCarbonReportEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCarbonReportEstimateValidator(): WeightedCarbonReportEstimateValidator {
    return WeightedCarbonReportEstimateValidator(
        mutableListOf<WeightedCarbonReportEstimateRule>(
            WeightedCarbonReportEstimateWindowDaysRangeRule(16, 1000),
            WeightedCarbonReportEstimateRouteCodeLengthRule(16),
            WeightedCarbonReportEstimateUpdatedAtRangeRule(120, 255),
        ),
    )
}
