package com.acme.transport.fleetops.serviceplan.planquota

/** A rule a service plan override must pass. Check returns its problems, or none. */
interface ServicePlanOverrideRule {
    fun check(item: ServicePlanOverride): MutableList<String>
}

data class ServicePlanOverrideSequenceRangeRule(var min: Int, var max: Int) : ServicePlanOverrideRule {
    override fun check(item: ServicePlanOverride): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class ServicePlanOverrideRequiredRouteCodeRule(var message: String) : ServicePlanOverrideRule {
    override fun check(item: ServicePlanOverride): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanOverrideCodeLengthRule(var maxLength: Int) : ServicePlanOverrideRule {
    override fun check(item: ServicePlanOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanOverrideBudgetRangeRule(var min: Double, var max: Double) : ServicePlanOverrideRule {
    override fun check(item: ServicePlanOverride): MutableList<String> {
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

/** Runs every rule of a service plan override. */
data class ServicePlanOverrideValidator(var rules: MutableList<ServicePlanOverrideRule>) {
    fun validate(item: ServicePlanOverride): MutableList<String> {
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

    fun requireValid(item: ServicePlanOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanOverrideValidator(): ServicePlanOverrideValidator {
    return ServicePlanOverrideValidator(
        mutableListOf<ServicePlanOverrideRule>(
            ServicePlanOverrideSequenceRangeRule(255, 1000),
            ServicePlanOverrideRequiredRouteCodeRule("service plan override needs a value"),
            ServicePlanOverrideCodeLengthRule(3600),
            ServicePlanOverrideBudgetRangeRule(6.4, 3627.5),
        ),
    )
}
