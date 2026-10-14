package com.acme.transport.emissions.carbonreport.reportmapping

/** A rule a strict carbon report mapping must pass. Check returns its problems, or none. */
interface StrictCarbonReportMappingRule {
    fun check(item: StrictCarbonReportMapping): MutableList<String>
}

data class StrictCarbonReportMappingLabelLengthRule(var maxLength: Int) : StrictCarbonReportMappingRule {
    override fun check(item: StrictCarbonReportMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictCarbonReportMappingCeilingRangeRule(
    var min: Double,
    var max: Double,
) : StrictCarbonReportMappingRule {
    override fun check(item: StrictCarbonReportMapping): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictCarbonReportMappingRequiredLabelRule(var message: String) : StrictCarbonReportMappingRule {
    override fun check(item: StrictCarbonReportMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict carbon report mapping. */
data class StrictCarbonReportMappingValidator(var rules: MutableList<StrictCarbonReportMappingRule>) {
    fun validate(item: StrictCarbonReportMapping): MutableList<String> {
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

    fun requireValid(item: StrictCarbonReportMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCarbonReportMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCarbonReportMappingValidator(): StrictCarbonReportMappingValidator {
    return StrictCarbonReportMappingValidator(
        mutableListOf<StrictCarbonReportMappingRule>(
            StrictCarbonReportMappingLabelLengthRule(32),
            StrictCarbonReportMappingCeilingRangeRule(2.2, 1690.2),
            StrictCarbonReportMappingRequiredLabelRule("strict carbon report mapping needs an entry"),
        ),
    )
}
