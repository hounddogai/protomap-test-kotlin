package com.acme.transport.emissions.carbonreport.adaptivereportrule

/** A rule a capped carbon report allocation must pass. Check returns its problems, or none. */
interface CappedCarbonReportAllocationRule {
    fun check(item: CappedCarbonReportAllocation): MutableList<String>
}

data class CappedCarbonReportAllocationOdometerRangeRule(
    var min: Int,
    var max: Int,
) : CappedCarbonReportAllocationRule {
    override fun check(item: CappedCarbonReportAllocation): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class CappedCarbonReportAllocationAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : CappedCarbonReportAllocationRule {
    override fun check(item: CappedCarbonReportAllocation): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportAllocationMaximumRangeRule(
    var min: Double,
    var max: Double,
) : CappedCarbonReportAllocationRule {
    override fun check(item: CappedCarbonReportAllocation): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped carbon report allocation. */
data class CappedCarbonReportAllocationValidator(var rules: MutableList<CappedCarbonReportAllocationRule>) {
    fun validate(item: CappedCarbonReportAllocation): MutableList<String> {
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

    fun requireValid(item: CappedCarbonReportAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCarbonReportAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCarbonReportAllocationValidator(): CappedCarbonReportAllocationValidator {
    return CappedCarbonReportAllocationValidator(
        mutableListOf<CappedCarbonReportAllocationRule>(
            CappedCarbonReportAllocationOdometerRangeRule(1000, 86400),
            CappedCarbonReportAllocationAttemptsRangeRule(16, 3600),
            CappedCarbonReportAllocationMaximumRangeRule(0.2, 3888.7),
        ),
    )
}
