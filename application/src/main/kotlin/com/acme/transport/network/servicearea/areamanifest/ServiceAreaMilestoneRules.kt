package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.text.normalize

/** A rule a service area milestone must pass. Check returns its problems, or none. */
interface ServiceAreaMilestoneRule {
    fun check(item: ServiceAreaMilestone): MutableList<String>
}

data class ServiceAreaMilestoneBudgetRangeRule(var min: Double, var max: Double) : ServiceAreaMilestoneRule {
    override fun check(item: ServiceAreaMilestone): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaMilestoneCodePrefixRule(var prefix: String) : ServiceAreaMilestoneRule {
    override fun check(item: ServiceAreaMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaMilestoneCostRangeRule(var min: Double, var max: Double) : ServiceAreaMilestoneRule {
    override fun check(item: ServiceAreaMilestone): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaMilestoneCodeLengthRule(var maxLength: Int) : ServiceAreaMilestoneRule {
    override fun check(item: ServiceAreaMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaMilestoneAllowedCodeRule(var allowed: MutableList<String>) : ServiceAreaMilestoneRule {
    override fun check(item: ServiceAreaMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaMilestoneRequiredVehicleIdRule(var message: String) : ServiceAreaMilestoneRule {
    override fun check(item: ServiceAreaMilestone): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area milestone. */
data class ServiceAreaMilestoneValidator(var rules: MutableList<ServiceAreaMilestoneRule>) {
    fun validate(item: ServiceAreaMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaMilestoneValidator(): ServiceAreaMilestoneValidator {
    return ServiceAreaMilestoneValidator(
        mutableListOf<ServiceAreaMilestoneRule>(
            ServiceAreaMilestoneBudgetRangeRule(2.0, 4759.7),
            ServiceAreaMilestoneCodePrefixRule("OPS"),
            ServiceAreaMilestoneCostRangeRule(2.6, 4111.1),
            ServiceAreaMilestoneCodeLengthRule(3600),
            ServiceAreaMilestoneAllowedCodeRule(mutableListOf<String>()),
            ServiceAreaMilestoneRequiredVehicleIdRule("service area milestone needs a value"),
        ),
    )
}
