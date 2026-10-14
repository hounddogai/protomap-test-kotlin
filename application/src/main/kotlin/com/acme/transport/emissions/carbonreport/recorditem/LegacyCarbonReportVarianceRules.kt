package com.acme.transport.emissions.carbonreport.recorditem

/** A rule a legacy carbon report variance must pass. Check returns its problems, or none. */
interface LegacyCarbonReportVarianceRule {
    fun check(item: LegacyCarbonReportVariance): MutableList<String>
}

data class LegacyCarbonReportVarianceDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCarbonReportVarianceRule {
    override fun check(item: LegacyCarbonReportVariance): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class LegacyCarbonReportVarianceCodeLengthRule(var maxLength: Int) : LegacyCarbonReportVarianceRule {
    override fun check(item: LegacyCarbonReportVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCarbonReportVarianceNameLengthRule(var maxLength: Int) : LegacyCarbonReportVarianceRule {
    override fun check(item: LegacyCarbonReportVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy carbon report variance. */
data class LegacyCarbonReportVarianceValidator(var rules: MutableList<LegacyCarbonReportVarianceRule>) {
    fun validate(item: LegacyCarbonReportVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCarbonReportVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCarbonReportVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCarbonReportVarianceValidator(): LegacyCarbonReportVarianceValidator {
    return LegacyCarbonReportVarianceValidator(
        mutableListOf<LegacyCarbonReportVarianceRule>(
            LegacyCarbonReportVarianceDiscountRangeRule(0.4, 2306.5),
            LegacyCarbonReportVarianceCodeLengthRule(255),
            LegacyCarbonReportVarianceNameLengthRule(64),
        ),
    )
}
