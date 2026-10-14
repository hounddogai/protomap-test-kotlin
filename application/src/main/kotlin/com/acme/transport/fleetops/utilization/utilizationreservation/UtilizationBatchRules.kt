package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.normalize

/** A rule a utilization batch must pass. Check returns its problems, or none. */
interface UtilizationBatchRule {
    fun check(item: UtilizationBatch): MutableList<String>
}

data class UtilizationBatchRequiredCodeRule(var message: String) : UtilizationBatchRule {
    override fun check(item: UtilizationBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationBatchAllowedCodeRule(var allowed: MutableList<String>) : UtilizationBatchRule {
    override fun check(item: UtilizationBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationBatchDiscountRangeRule(var min: Double, var max: Double) : UtilizationBatchRule {
    override fun check(item: UtilizationBatch): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization batch. */
data class UtilizationBatchValidator(var rules: MutableList<UtilizationBatchRule>) {
    fun validate(item: UtilizationBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationBatchValidator(): UtilizationBatchValidator {
    return UtilizationBatchValidator(
        mutableListOf<UtilizationBatchRule>(
            UtilizationBatchRequiredCodeRule("utilization batch needs an entry"),
            UtilizationBatchAllowedCodeRule(mutableListOf<String>()),
            UtilizationBatchDiscountRangeRule(2.3, 2299.5),
        ),
    )
}
