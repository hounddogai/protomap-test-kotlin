package com.acme.transport.fleetops.serviceplan.estimate

/** A rule a service plan allocation must pass. Check returns its problems, or none. */
interface ServicePlanAllocationRule {
    fun check(item: ServicePlanAllocation): MutableList<String>
}

data class ServicePlanAllocationBatchSizeRangeRule(var min: Int, var max: Int) : ServicePlanAllocationRule {
    override fun check(item: ServicePlanAllocation): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAllocationCodePrefixRule(var prefix: String) : ServicePlanAllocationRule {
    override fun check(item: ServicePlanAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAllocationRequiredLabelRule(var message: String) : ServicePlanAllocationRule {
    override fun check(item: ServicePlanAllocation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan allocation. */
data class ServicePlanAllocationValidator(var rules: MutableList<ServicePlanAllocationRule>) {
    fun validate(item: ServicePlanAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanAllocationValidator(): ServicePlanAllocationValidator {
    return ServicePlanAllocationValidator(
        mutableListOf<ServicePlanAllocationRule>(
            ServicePlanAllocationBatchSizeRangeRule(1000, 3600),
            ServicePlanAllocationCodePrefixRule("X"),
            ServicePlanAllocationRequiredLabelRule("service plan allocation needs a value"),
        ),
    )
}
