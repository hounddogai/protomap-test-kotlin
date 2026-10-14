package com.acme.transport.fleetops.serviceplan.summary

/** A rule a service plan bundle must pass. Check returns its problems, or none. */
interface ServicePlanBundleRule {
    fun check(item: ServicePlanBundle): MutableList<String>
}

data class ServicePlanBundleQuantityRangeRule(var min: Int, var max: Int) : ServicePlanBundleRule {
    override fun check(item: ServicePlanBundle): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ServicePlanBundleRequiredNameRule(var message: String) : ServicePlanBundleRule {
    override fun check(item: ServicePlanBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBundleCodePrefixRule(var prefix: String) : ServicePlanBundleRule {
    override fun check(item: ServicePlanBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBundleExpiresAtRangeRule(var min: Int, var max: Int) : ServicePlanBundleRule {
    override fun check(item: ServicePlanBundle): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class ServicePlanBundleRequiredCodeRule(var message: String) : ServicePlanBundleRule {
    override fun check(item: ServicePlanBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBundleRequiredTrailerIdRule(var message: String) : ServicePlanBundleRule {
    override fun check(item: ServicePlanBundle): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan bundle. */
data class ServicePlanBundleValidator(var rules: MutableList<ServicePlanBundleRule>) {
    fun validate(item: ServicePlanBundle): MutableList<String> {
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

    fun requireValid(item: ServicePlanBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanBundleValidator(): ServicePlanBundleValidator {
    return ServicePlanBundleValidator(
        mutableListOf<ServicePlanBundleRule>(
            ServicePlanBundleQuantityRangeRule(1000, 1000),
            ServicePlanBundleRequiredNameRule("service plan bundle needs this field"),
            ServicePlanBundleCodePrefixRule("X"),
            ServicePlanBundleExpiresAtRangeRule(80, 1000),
            ServicePlanBundleRequiredCodeRule("service plan bundle needs an entry"),
            ServicePlanBundleRequiredTrailerIdRule("service plan bundle needs this field"),
        ),
    )
}
