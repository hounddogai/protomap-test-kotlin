package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.text.normalize

/** A rule a batched service plan summary must pass. Check returns its problems, or none. */
interface BatchedServicePlanSummaryRule {
    fun check(item: BatchedServicePlanSummary): MutableList<String>
}

data class BatchedServicePlanSummaryRequiredSummaryRule(var message: String) : BatchedServicePlanSummaryRule {
    override fun check(item: BatchedServicePlanSummary): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanSummaryAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedServicePlanSummaryRule {
    override fun check(item: BatchedServicePlanSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanSummaryVehicleIdLengthRule(var maxLength: Int) : BatchedServicePlanSummaryRule {
    override fun check(item: BatchedServicePlanSummary): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanSummaryCodePrefixRule(var prefix: String) : BatchedServicePlanSummaryRule {
    override fun check(item: BatchedServicePlanSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanSummaryCodeLengthRule(var maxLength: Int) : BatchedServicePlanSummaryRule {
    override fun check(item: BatchedServicePlanSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanSummaryCostRangeRule(
    var min: Double,
    var max: Double,
) : BatchedServicePlanSummaryRule {
    override fun check(item: BatchedServicePlanSummary): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched service plan summary. */
data class BatchedServicePlanSummaryValidator(var rules: MutableList<BatchedServicePlanSummaryRule>) {
    fun validate(item: BatchedServicePlanSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedServicePlanSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedServicePlanSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedServicePlanSummaryValidator(): BatchedServicePlanSummaryValidator {
    return BatchedServicePlanSummaryValidator(
        mutableListOf<BatchedServicePlanSummaryRule>(
            BatchedServicePlanSummaryRequiredSummaryRule("batched service plan summary needs this field"),
            BatchedServicePlanSummaryAllowedCodeRule(mutableListOf<String>("global", "scheduled", "fallback")),
            BatchedServicePlanSummaryVehicleIdLengthRule(32),
            BatchedServicePlanSummaryCodePrefixRule("REF"),
            BatchedServicePlanSummaryCodeLengthRule(16),
            BatchedServicePlanSummaryCostRangeRule(8.0, 1128.5),
        ),
    )
}
