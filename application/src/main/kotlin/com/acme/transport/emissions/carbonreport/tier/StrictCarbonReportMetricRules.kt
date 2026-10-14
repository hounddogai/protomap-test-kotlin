package com.acme.transport.emissions.carbonreport.tier

/** A rule a strict carbon report metric must pass. Check returns its problems, or none. */
interface StrictCarbonReportMetricRule {
    fun check(item: StrictCarbonReportMetric): MutableList<String>
}

data class StrictCarbonReportMetricPalletsRangeRule(var min: Int, var max: Int) : StrictCarbonReportMetricRule {
    override fun check(item: StrictCarbonReportMetric): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictCarbonReportMetricRequiredTitleRule(var message: String) : StrictCarbonReportMetricRule {
    override fun check(item: StrictCarbonReportMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCarbonReportMetricCodeLengthRule(var maxLength: Int) : StrictCarbonReportMetricRule {
    override fun check(item: StrictCarbonReportMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictCarbonReportMetricPhoneLengthRule(var maxLength: Int) : StrictCarbonReportMetricRule {
    override fun check(item: StrictCarbonReportMetric): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict carbon report metric. */
data class StrictCarbonReportMetricValidator(var rules: MutableList<StrictCarbonReportMetricRule>) {
    fun validate(item: StrictCarbonReportMetric): MutableList<String> {
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

    fun requireValid(item: StrictCarbonReportMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCarbonReportMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCarbonReportMetricValidator(): StrictCarbonReportMetricValidator {
    return StrictCarbonReportMetricValidator(
        mutableListOf<StrictCarbonReportMetricRule>(
            StrictCarbonReportMetricPalletsRangeRule(32, 86400),
            StrictCarbonReportMetricRequiredTitleRule("strict carbon report metric needs this field"),
            StrictCarbonReportMetricCodeLengthRule(64),
            StrictCarbonReportMetricPhoneLengthRule(80),
        ),
    )
}
