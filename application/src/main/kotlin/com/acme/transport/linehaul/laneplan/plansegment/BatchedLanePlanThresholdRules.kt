package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.text.normalize

/** A rule a batched lane plan threshold must pass. Check returns its problems, or none. */
interface BatchedLanePlanThresholdRule {
    fun check(item: BatchedLanePlanThreshold): MutableList<String>
}

data class BatchedLanePlanThresholdRequiredVehicleIdRule(var message: String) : BatchedLanePlanThresholdRule {
    override fun check(item: BatchedLanePlanThreshold): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedLanePlanThresholdAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : BatchedLanePlanThresholdRule {
    override fun check(item: BatchedLanePlanThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLanePlanThresholdCodePrefixRule(var prefix: String) : BatchedLanePlanThresholdRule {
    override fun check(item: BatchedLanePlanThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLanePlanThresholdRequiredCategoryRule(var message: String) : BatchedLanePlanThresholdRule {
    override fun check(item: BatchedLanePlanThreshold): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedLanePlanThresholdRequiredCodeRule(var message: String) : BatchedLanePlanThresholdRule {
    override fun check(item: BatchedLanePlanThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched lane plan threshold. */
data class BatchedLanePlanThresholdValidator(var rules: MutableList<BatchedLanePlanThresholdRule>) {
    fun validate(item: BatchedLanePlanThreshold): MutableList<String> {
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

    fun requireValid(item: BatchedLanePlanThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedLanePlanThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedLanePlanThresholdValidator(): BatchedLanePlanThresholdValidator {
    return BatchedLanePlanThresholdValidator(
        mutableListOf<BatchedLanePlanThresholdRule>(
            BatchedLanePlanThresholdRequiredVehicleIdRule("batched lane plan threshold needs an entry"),
            BatchedLanePlanThresholdAllowedVehicleIdRule(mutableListOf<String>("pending")),
            BatchedLanePlanThresholdCodePrefixRule("ACM"),
            BatchedLanePlanThresholdRequiredCategoryRule("batched lane plan threshold needs a value"),
            BatchedLanePlanThresholdRequiredCodeRule("batched lane plan threshold needs an entry"),
        ),
    )
}
