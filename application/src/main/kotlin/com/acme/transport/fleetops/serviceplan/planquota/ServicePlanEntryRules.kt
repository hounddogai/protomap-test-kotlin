package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.text.normalize

/** A rule a service plan entry must pass. Check returns its problems, or none. */
interface ServicePlanEntryRule {
    fun check(item: ServicePlanEntry): MutableList<String>
}

data class ServicePlanEntryRequiredCurrencyRule(var message: String) : ServicePlanEntryRule {
    override fun check(item: ServicePlanEntry): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanEntryRevisionRangeRule(var min: Int, var max: Int) : ServicePlanEntryRule {
    override fun check(item: ServicePlanEntry): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanEntryRequiredCodeRule(var message: String) : ServicePlanEntryRule {
    override fun check(item: ServicePlanEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanEntryAllowedLaneRule(var allowed: MutableList<String>) : ServicePlanEntryRule {
    override fun check(item: ServicePlanEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanEntryRequiredLaneRule(var message: String) : ServicePlanEntryRule {
    override fun check(item: ServicePlanEntry): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan entry. */
data class ServicePlanEntryValidator(var rules: MutableList<ServicePlanEntryRule>) {
    fun validate(item: ServicePlanEntry): MutableList<String> {
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

    fun requireValid(item: ServicePlanEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanEntryValidator(): ServicePlanEntryValidator {
    return ServicePlanEntryValidator(
        mutableListOf<ServicePlanEntryRule>(
            ServicePlanEntryRequiredCurrencyRule("service plan entry needs an entry"),
            ServicePlanEntryRevisionRangeRule(80, 1000),
            ServicePlanEntryRequiredCodeRule("service plan entry needs a value"),
            ServicePlanEntryAllowedLaneRule(mutableListOf<String>("pending", "rolling", "global")),
            ServicePlanEntryRequiredLaneRule("service plan entry needs this field"),
        ),
    )
}
