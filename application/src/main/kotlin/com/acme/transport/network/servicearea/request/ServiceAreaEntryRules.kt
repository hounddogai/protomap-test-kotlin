package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.normalize

/** A rule a service area entry must pass. Check returns its problems, or none. */
interface ServiceAreaEntryRule {
    fun check(item: ServiceAreaEntry): MutableList<String>
}

data class ServiceAreaEntryAllowedCodeRule(var allowed: MutableList<String>) : ServiceAreaEntryRule {
    override fun check(item: ServiceAreaEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaEntryRequiredTimezoneRule(var message: String) : ServiceAreaEntryRule {
    override fun check(item: ServiceAreaEntry): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaEntryBudgetRangeRule(var min: Double, var max: Double) : ServiceAreaEntryRule {
    override fun check(item: ServiceAreaEntry): MutableList<String> {
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

data class ServiceAreaEntryRequiredLabelRule(var message: String) : ServiceAreaEntryRule {
    override fun check(item: ServiceAreaEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area entry. */
data class ServiceAreaEntryValidator(var rules: MutableList<ServiceAreaEntryRule>) {
    fun validate(item: ServiceAreaEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaEntryValidator(): ServiceAreaEntryValidator {
    return ServiceAreaEntryValidator(
        mutableListOf<ServiceAreaEntryRule>(
            ServiceAreaEntryAllowedCodeRule(mutableListOf<String>("rolling", "scheduled", "batched")),
            ServiceAreaEntryRequiredTimezoneRule("service area entry needs a value"),
            ServiceAreaEntryBudgetRangeRule(7.1, 3643.5),
            ServiceAreaEntryRequiredLabelRule("service area entry needs this field"),
        ),
    )
}
