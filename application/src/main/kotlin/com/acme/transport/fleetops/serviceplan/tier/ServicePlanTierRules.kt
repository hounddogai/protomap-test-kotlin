package com.acme.transport.fleetops.serviceplan.tier

/** A rule a service plan tier must pass. Check returns its problems, or none. */
interface ServicePlanTierRule {
    fun check(item: ServicePlanTier): MutableList<String>
}

data class ServicePlanTierCodePrefixRule(var prefix: String) : ServicePlanTierRule {
    override fun check(item: ServicePlanTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTierRouteCodeLengthRule(var maxLength: Int) : ServicePlanTierRule {
    override fun check(item: ServicePlanTier): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTierRequiredCodeRule(var message: String) : ServicePlanTierRule {
    override fun check(item: ServicePlanTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTierRequiredRouteCodeRule(var message: String) : ServicePlanTierRule {
    override fun check(item: ServicePlanTier): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan tier. */
data class ServicePlanTierValidator(var rules: MutableList<ServicePlanTierRule>) {
    fun validate(item: ServicePlanTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanTier>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanTierValidator(): ServicePlanTierValidator {
    return ServicePlanTierValidator(
        mutableListOf<ServicePlanTierRule>(
            ServicePlanTierCodePrefixRule("Q"),
            ServicePlanTierRouteCodeLengthRule(80),
            ServicePlanTierRequiredCodeRule("service plan tier needs a value"),
            ServicePlanTierRequiredRouteCodeRule("service plan tier needs this field"),
        ),
    )
}
