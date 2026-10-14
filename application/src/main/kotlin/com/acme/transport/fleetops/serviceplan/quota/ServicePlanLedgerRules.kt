package com.acme.transport.fleetops.serviceplan.quota

import com.acme.transport.kit.text.normalize

/** A rule a service plan ledger must pass. Check returns its problems, or none. */
interface ServicePlanLedgerRule {
    fun check(item: ServicePlanLedger): MutableList<String>
}

data class ServicePlanLedgerUpdatedAtRangeRule(var min: Int, var max: Int) : ServicePlanLedgerRule {
    override fun check(item: ServicePlanLedger): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class ServicePlanLedgerCodePrefixRule(var prefix: String) : ServicePlanLedgerRule {
    override fun check(item: ServicePlanLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanLedgerAllowedLabelRule(var allowed: MutableList<String>) : ServicePlanLedgerRule {
    override fun check(item: ServicePlanLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanLedgerCodeLengthRule(var maxLength: Int) : ServicePlanLedgerRule {
    override fun check(item: ServicePlanLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan ledger. */
data class ServicePlanLedgerValidator(var rules: MutableList<ServicePlanLedgerRule>) {
    fun validate(item: ServicePlanLedger): MutableList<String> {
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

    fun requireValid(item: ServicePlanLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanLedgerValidator(): ServicePlanLedgerValidator {
    return ServicePlanLedgerValidator(
        mutableListOf<ServicePlanLedgerRule>(
            ServicePlanLedgerUpdatedAtRangeRule(255, 255),
            ServicePlanLedgerCodePrefixRule("OPS"),
            ServicePlanLedgerAllowedLabelRule(mutableListOf<String>("tiered")),
            ServicePlanLedgerCodeLengthRule(16),
        ),
    )
}
