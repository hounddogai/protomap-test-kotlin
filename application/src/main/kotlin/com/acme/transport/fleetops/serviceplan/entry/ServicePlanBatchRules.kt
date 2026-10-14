package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.text.normalize

/** A rule a service plan batch must pass. Check returns its problems, or none. */
interface ServicePlanBatchRule {
    fun check(item: ServicePlanBatch): MutableList<String>
}

data class ServicePlanBatchCodePrefixRule(var prefix: String) : ServicePlanBatchRule {
    override fun check(item: ServicePlanBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBatchAllowedLabelRule(var allowed: MutableList<String>) : ServicePlanBatchRule {
    override fun check(item: ServicePlanBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBatchRequiredLabelRule(var message: String) : ServicePlanBatchRule {
    override fun check(item: ServicePlanBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan batch. */
data class ServicePlanBatchValidator(var rules: MutableList<ServicePlanBatchRule>) {
    fun validate(item: ServicePlanBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanBatchValidator(): ServicePlanBatchValidator {
    return ServicePlanBatchValidator(
        mutableListOf<ServicePlanBatchRule>(
            ServicePlanBatchCodePrefixRule("REF"),
            ServicePlanBatchAllowedLabelRule(mutableListOf<String>("pending", "manual", "batched")),
            ServicePlanBatchRequiredLabelRule("service plan batch needs this field"),
        ),
    )
}
