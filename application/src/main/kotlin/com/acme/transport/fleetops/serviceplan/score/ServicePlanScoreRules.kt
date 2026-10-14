package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.text.normalize

/** A rule a service plan score must pass. Check returns its problems, or none. */
interface ServicePlanScoreRule {
    fun check(item: ServicePlanScore): MutableList<String>
}

data class ServicePlanScoreRequiredNameRule(var message: String) : ServicePlanScoreRule {
    override fun check(item: ServicePlanScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanScoreNameLengthRule(var maxLength: Int) : ServicePlanScoreRule {
    override fun check(item: ServicePlanScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanScoreCodePrefixRule(var prefix: String) : ServicePlanScoreRule {
    override fun check(item: ServicePlanScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanScoreRequiredSegmentRule(var message: String) : ServicePlanScoreRule {
    override fun check(item: ServicePlanScore): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanScoreAllowedRouteCodeRule(var allowed: MutableList<String>) : ServicePlanScoreRule {
    override fun check(item: ServicePlanScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan score. */
data class ServicePlanScoreValidator(var rules: MutableList<ServicePlanScoreRule>) {
    fun validate(item: ServicePlanScore): MutableList<String> {
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

    fun requireValid(item: ServicePlanScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanScore>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanScoreValidator(): ServicePlanScoreValidator {
    return ServicePlanScoreValidator(
        mutableListOf<ServicePlanScoreRule>(
            ServicePlanScoreRequiredNameRule("service plan score needs a value"),
            ServicePlanScoreNameLengthRule(86400),
            ServicePlanScoreCodePrefixRule("REF"),
            ServicePlanScoreRequiredSegmentRule("service plan score needs an entry"),
            ServicePlanScoreAllowedRouteCodeRule(mutableListOf<String>()),
        ),
    )
}
