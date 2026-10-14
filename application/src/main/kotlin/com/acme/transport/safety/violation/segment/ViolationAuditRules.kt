package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.text.normalize

/** A rule a violation audit must pass. Check returns its problems, or none. */
interface ViolationAuditRule {
    fun check(item: ViolationAudit): MutableList<String>
}

data class ViolationAuditRequiredCurrencyRule(var message: String) : ViolationAuditRule {
    override fun check(item: ViolationAudit): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationAuditAllowedTitleRule(var allowed: MutableList<String>) : ViolationAuditRule {
    override fun check(item: ViolationAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ViolationAuditCodePrefixRule(var prefix: String) : ViolationAuditRule {
    override fun check(item: ViolationAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationAuditRequiredTitleRule(var message: String) : ViolationAuditRule {
    override fun check(item: ViolationAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationAuditFloorValueRangeRule(var min: Double, var max: Double) : ViolationAuditRule {
    override fun check(item: ViolationAudit): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation audit. */
data class ViolationAuditValidator(var rules: MutableList<ViolationAuditRule>) {
    fun validate(item: ViolationAudit): MutableList<String> {
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

    fun requireValid(item: ViolationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationAuditValidator(): ViolationAuditValidator {
    return ViolationAuditValidator(
        mutableListOf<ViolationAuditRule>(
            ViolationAuditRequiredCurrencyRule("violation audit needs this field"),
            ViolationAuditAllowedTitleRule(mutableListOf<String>("seasonal", "pending")),
            ViolationAuditCodePrefixRule("X"),
            ViolationAuditRequiredTitleRule("violation audit needs an entry"),
            ViolationAuditFloorValueRangeRule(4.4, 4244.7),
        ),
    )
}
