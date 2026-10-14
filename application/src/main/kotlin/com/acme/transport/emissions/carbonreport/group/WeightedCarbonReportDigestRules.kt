package com.acme.transport.emissions.carbonreport.group

/** A rule a weighted carbon report digest must pass. Check returns its problems, or none. */
interface WeightedCarbonReportDigestRule {
    fun check(item: WeightedCarbonReportDigest): MutableList<String>
}

data class WeightedCarbonReportDigestCeilingRangeRule(
    var min: Double,
    var max: Double,
) : WeightedCarbonReportDigestRule {
    override fun check(item: WeightedCarbonReportDigest): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class WeightedCarbonReportDigestRequiredCodeRule(var message: String) : WeightedCarbonReportDigestRule {
    override fun check(item: WeightedCarbonReportDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportDigestUnitLengthRule(var maxLength: Int) : WeightedCarbonReportDigestRule {
    override fun check(item: WeightedCarbonReportDigest): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted carbon report digest. */
data class WeightedCarbonReportDigestValidator(var rules: MutableList<WeightedCarbonReportDigestRule>) {
    fun validate(item: WeightedCarbonReportDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCarbonReportDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCarbonReportDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCarbonReportDigestValidator(): WeightedCarbonReportDigestValidator {
    return WeightedCarbonReportDigestValidator(
        mutableListOf<WeightedCarbonReportDigestRule>(
            WeightedCarbonReportDigestCeilingRangeRule(9.4, 1724.4),
            WeightedCarbonReportDigestRequiredCodeRule("weighted carbon report digest needs a value"),
            WeightedCarbonReportDigestUnitLengthRule(3600),
        ),
    )
}
