package com.acme.transport.fleetops.serviceplan.summary

/** A rule a service plan mapping must pass. Check returns its problems, or none. */
interface ServicePlanMappingRule {
    fun check(item: ServicePlanMapping): MutableList<String>
}

data class ServicePlanMappingCodeLengthRule(var maxLength: Int) : ServicePlanMappingRule {
    override fun check(item: ServicePlanMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanMappingRequiredUnitRule(var message: String) : ServicePlanMappingRule {
    override fun check(item: ServicePlanMapping): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanMappingRequiredCodeRule(var message: String) : ServicePlanMappingRule {
    override fun check(item: ServicePlanMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan mapping. */
data class ServicePlanMappingValidator(var rules: MutableList<ServicePlanMappingRule>) {
    fun validate(item: ServicePlanMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanMappingValidator(): ServicePlanMappingValidator {
    return ServicePlanMappingValidator(
        mutableListOf<ServicePlanMappingRule>(
            ServicePlanMappingCodeLengthRule(64),
            ServicePlanMappingRequiredUnitRule("service plan mapping needs this field"),
            ServicePlanMappingRequiredCodeRule("service plan mapping needs this field"),
        ),
    )
}
