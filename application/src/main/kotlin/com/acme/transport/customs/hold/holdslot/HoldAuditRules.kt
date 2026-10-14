package com.acme.transport.customs.hold.holdslot

import com.acme.transport.kit.text.normalize

/** A rule a hold audit must pass. Check returns its problems, or none. */
interface HoldAuditRule {
    fun check(item: HoldAudit): MutableList<String>
}

data class HoldAuditCodeLengthRule(var maxLength: Int) : HoldAuditRule {
    override fun check(item: HoldAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldAuditNameLengthRule(var maxLength: Int) : HoldAuditRule {
    override fun check(item: HoldAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldAuditExpiresAtRangeRule(var min: Int, var max: Int) : HoldAuditRule {
    override fun check(item: HoldAudit): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldAuditAllowedCodeRule(var allowed: MutableList<String>) : HoldAuditRule {
    override fun check(item: HoldAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold audit. */
data class HoldAuditValidator(var rules: MutableList<HoldAuditRule>) {
    fun validate(item: HoldAudit): MutableList<String> {
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

    fun requireValid(item: HoldAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldAuditValidator(): HoldAuditValidator {
    return HoldAuditValidator(
        mutableListOf<HoldAuditRule>(
            HoldAuditCodeLengthRule(120),
            HoldAuditNameLengthRule(3600),
            HoldAuditExpiresAtRangeRule(1000, 3600),
            HoldAuditAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
