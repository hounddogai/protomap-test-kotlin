package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.text.normalize

/** A rule a carbon report mapping must pass. Check returns its problems, or none. */
interface CarbonReportMappingRule {
    fun check(item: CarbonReportMapping): MutableList<String>
}

data class CarbonReportMappingAllowedTitleRule(var allowed: MutableList<String>) : CarbonReportMappingRule {
    override fun check(item: CarbonReportMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportMappingMaximumRangeRule(var min: Double, var max: Double) : CarbonReportMappingRule {
    override fun check(item: CarbonReportMapping): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportMappingRetriesRangeRule(var min: Int, var max: Int) : CarbonReportMappingRule {
    override fun check(item: CarbonReportMapping): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a carbon report mapping. */
data class CarbonReportMappingValidator(var rules: MutableList<CarbonReportMappingRule>) {
    fun validate(item: CarbonReportMapping): MutableList<String> {
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

    fun requireValid(item: CarbonReportMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportMappingValidator(): CarbonReportMappingValidator {
    return CarbonReportMappingValidator(
        mutableListOf<CarbonReportMappingRule>(
            CarbonReportMappingAllowedTitleRule(mutableListOf<String>("batched", "seasonal")),
            CarbonReportMappingMaximumRangeRule(9.6, 597.1),
            CarbonReportMappingRetriesRangeRule(64, 86400),
        ),
    )
}
