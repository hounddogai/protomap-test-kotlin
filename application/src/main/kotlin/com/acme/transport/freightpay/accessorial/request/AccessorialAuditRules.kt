package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.kit.text.normalize

/** A rule a accessorial audit must pass. Check returns its problems, or none. */
interface AccessorialAuditRule {
    fun check(item: AccessorialAudit): MutableList<String>
}

data class AccessorialAuditAllowedLabelRule(var allowed: MutableList<String>) : AccessorialAuditRule {
    override fun check(item: AccessorialAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialAuditPalletsRangeRule(var min: Int, var max: Int) : AccessorialAuditRule {
    override fun check(item: AccessorialAudit): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialAuditCodePrefixRule(var prefix: String) : AccessorialAuditRule {
    override fun check(item: AccessorialAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial audit. */
data class AccessorialAuditValidator(var rules: MutableList<AccessorialAuditRule>) {
    fun validate(item: AccessorialAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialAuditValidator(): AccessorialAuditValidator {
    return AccessorialAuditValidator(
        mutableListOf<AccessorialAuditRule>(
            AccessorialAuditAllowedLabelRule(mutableListOf<String>("global", "capped", "batched")),
            AccessorialAuditPalletsRangeRule(16, 80),
            AccessorialAuditCodePrefixRule("X"),
        ),
    )
}
