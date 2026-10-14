package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.text.normalize

/** A rule a carbon report budget must pass. Check returns its problems, or none. */
interface CarbonReportBudgetRule {
    fun check(item: CarbonReportBudget): MutableList<String>
}

data class CarbonReportBudgetAllowedCodeRule(var allowed: MutableList<String>) : CarbonReportBudgetRule {
    override fun check(item: CarbonReportBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportBudgetBatchSizeRangeRule(var min: Int, var max: Int) : CarbonReportBudgetRule {
    override fun check(item: CarbonReportBudget): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportBudgetFloorValueRangeRule(var min: Double, var max: Double) : CarbonReportBudgetRule {
    override fun check(item: CarbonReportBudget): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report budget. */
data class CarbonReportBudgetValidator(var rules: MutableList<CarbonReportBudgetRule>) {
    fun validate(item: CarbonReportBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportBudgetValidator(): CarbonReportBudgetValidator {
    return CarbonReportBudgetValidator(
        mutableListOf<CarbonReportBudgetRule>(
            CarbonReportBudgetAllowedCodeRule(mutableListOf<String>("lenient")),
            CarbonReportBudgetBatchSizeRangeRule(32, 64),
            CarbonReportBudgetFloorValueRangeRule(4.0, 3856.0),
        ),
    )
}
