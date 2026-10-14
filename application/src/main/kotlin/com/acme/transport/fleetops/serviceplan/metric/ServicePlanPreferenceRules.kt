package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.kit.text.normalize

/** A rule a service plan preference must pass. Check returns its problems, or none. */
interface ServicePlanPreferenceRule {
    fun check(item: ServicePlanPreference): MutableList<String>
}

data class ServicePlanPreferenceAllowedNameRule(var allowed: MutableList<String>) : ServicePlanPreferenceRule {
    override fun check(item: ServicePlanPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanPreferenceRequiredCodeRule(var message: String) : ServicePlanPreferenceRule {
    override fun check(item: ServicePlanPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanPreferenceRequiredSummaryRule(var message: String) : ServicePlanPreferenceRule {
    override fun check(item: ServicePlanPreference): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanPreferenceCapacityRangeRule(var min: Int, var max: Int) : ServicePlanPreferenceRule {
    override fun check(item: ServicePlanPreference): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class ServicePlanPreferenceRequiredNameRule(var message: String) : ServicePlanPreferenceRule {
    override fun check(item: ServicePlanPreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan preference. */
data class ServicePlanPreferenceValidator(var rules: MutableList<ServicePlanPreferenceRule>) {
    fun validate(item: ServicePlanPreference): MutableList<String> {
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

    fun requireValid(item: ServicePlanPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanPreferenceValidator(): ServicePlanPreferenceValidator {
    return ServicePlanPreferenceValidator(
        mutableListOf<ServicePlanPreferenceRule>(
            ServicePlanPreferenceAllowedNameRule(mutableListOf<String>("scheduled", "default", "global")),
            ServicePlanPreferenceRequiredCodeRule("service plan preference needs an entry"),
            ServicePlanPreferenceRequiredSummaryRule("service plan preference needs a value"),
            ServicePlanPreferenceCapacityRangeRule(32, 1000),
            ServicePlanPreferenceRequiredNameRule("service plan preference needs this field"),
        ),
    )
}
