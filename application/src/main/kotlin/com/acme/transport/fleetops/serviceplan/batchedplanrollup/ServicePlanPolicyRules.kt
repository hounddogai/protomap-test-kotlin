package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.text.normalize

/** A rule a service plan policy must pass. Check returns its problems, or none. */
interface ServicePlanPolicyRule {
    fun check(item: ServicePlanPolicy): MutableList<String>
}

data class ServicePlanPolicyFloorValueRangeRule(var min: Double, var max: Double) : ServicePlanPolicyRule {
    override fun check(item: ServicePlanPolicy): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanPolicyAllowedCodeRule(var allowed: MutableList<String>) : ServicePlanPolicyRule {
    override fun check(item: ServicePlanPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanPolicyRequiredTitleRule(var message: String) : ServicePlanPolicyRule {
    override fun check(item: ServicePlanPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanPolicyRequiredOwnerIdRule(var message: String) : ServicePlanPolicyRule {
    override fun check(item: ServicePlanPolicy): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanPolicyLabelLengthRule(var maxLength: Int) : ServicePlanPolicyRule {
    override fun check(item: ServicePlanPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan policy. */
data class ServicePlanPolicyValidator(var rules: MutableList<ServicePlanPolicyRule>) {
    fun validate(item: ServicePlanPolicy): MutableList<String> {
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

    fun requireValid(item: ServicePlanPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanPolicyValidator(): ServicePlanPolicyValidator {
    return ServicePlanPolicyValidator(
        mutableListOf<ServicePlanPolicyRule>(
            ServicePlanPolicyFloorValueRangeRule(1.3, 3140.9),
            ServicePlanPolicyAllowedCodeRule(mutableListOf<String>("rolling", "primary", "regional")),
            ServicePlanPolicyRequiredTitleRule("service plan policy needs this field"),
            ServicePlanPolicyRequiredOwnerIdRule("service plan policy needs this field"),
            ServicePlanPolicyLabelLengthRule(86400),
        ),
    )
}
