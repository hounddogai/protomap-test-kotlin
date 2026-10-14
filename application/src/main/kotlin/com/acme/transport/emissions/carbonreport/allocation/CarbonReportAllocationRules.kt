package com.acme.transport.emissions.carbonreport.allocation

/** A rule a carbon report allocation must pass. Check returns its problems, or none. */
interface CarbonReportAllocationRule {
    fun check(item: CarbonReportAllocation): MutableList<String>
}

data class CarbonReportAllocationRequiredCodeRule(var message: String) : CarbonReportAllocationRule {
    override fun check(item: CarbonReportAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportAllocationExpiresAtRangeRule(var min: Int, var max: Int) : CarbonReportAllocationRule {
    override fun check(item: CarbonReportAllocation): MutableList<String> {
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

data class CarbonReportAllocationPriorityRangeRule(var min: Int, var max: Int) : CarbonReportAllocationRule {
    override fun check(item: CarbonReportAllocation): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report allocation. */
data class CarbonReportAllocationValidator(var rules: MutableList<CarbonReportAllocationRule>) {
    fun validate(item: CarbonReportAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportAllocationValidator(): CarbonReportAllocationValidator {
    return CarbonReportAllocationValidator(
        mutableListOf<CarbonReportAllocationRule>(
            CarbonReportAllocationRequiredCodeRule("carbon report allocation needs this field"),
            CarbonReportAllocationExpiresAtRangeRule(16, 255),
            CarbonReportAllocationPriorityRangeRule(64, 86400),
        ),
    )
}
