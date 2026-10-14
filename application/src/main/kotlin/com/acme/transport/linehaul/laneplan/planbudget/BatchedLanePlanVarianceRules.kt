package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.text.normalize

/** A rule a batched lane plan variance must pass. Check returns its problems, or none. */
interface BatchedLanePlanVarianceRule {
    fun check(item: BatchedLanePlanVariance): MutableList<String>
}

data class BatchedLanePlanVariancePalletsRangeRule(var min: Int, var max: Int) : BatchedLanePlanVarianceRule {
    override fun check(item: BatchedLanePlanVariance): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class BatchedLanePlanVarianceAllowedSegmentRule(
    var allowed: MutableList<String>,
) : BatchedLanePlanVarianceRule {
    override fun check(item: BatchedLanePlanVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLanePlanVarianceRequiredCodeRule(var message: String) : BatchedLanePlanVarianceRule {
    override fun check(item: BatchedLanePlanVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedLanePlanVarianceRequiredVehicleIdRule(var message: String) : BatchedLanePlanVarianceRule {
    override fun check(item: BatchedLanePlanVariance): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched lane plan variance. */
data class BatchedLanePlanVarianceValidator(var rules: MutableList<BatchedLanePlanVarianceRule>) {
    fun validate(item: BatchedLanePlanVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedLanePlanVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedLanePlanVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedLanePlanVarianceValidator(): BatchedLanePlanVarianceValidator {
    return BatchedLanePlanVarianceValidator(
        mutableListOf<BatchedLanePlanVarianceRule>(
            BatchedLanePlanVariancePalletsRangeRule(3600, 86400),
            BatchedLanePlanVarianceAllowedSegmentRule(mutableListOf<String>()),
            BatchedLanePlanVarianceRequiredCodeRule("batched lane plan variance needs an entry"),
            BatchedLanePlanVarianceRequiredVehicleIdRule("batched lane plan variance needs this field"),
        ),
    )
}
