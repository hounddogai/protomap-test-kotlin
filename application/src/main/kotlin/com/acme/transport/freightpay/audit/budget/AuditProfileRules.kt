package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.normalize

/** A rule a audit profile must pass. Check returns its problems, or none. */
interface AuditProfileRule {
    fun check(item: AuditProfile): MutableList<String>
}

data class AuditProfileCodeLengthRule(var maxLength: Int) : AuditProfileRule {
    override fun check(item: AuditProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditProfileExpiresAtRangeRule(var min: Int, var max: Int) : AuditProfileRule {
    override fun check(item: AuditProfile): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditProfileAllowedDepotRule(var allowed: MutableList<String>) : AuditProfileRule {
    override fun check(item: AuditProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit profile. */
data class AuditProfileValidator(var rules: MutableList<AuditProfileRule>) {
    fun validate(item: AuditProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditProfileValidator(): AuditProfileValidator {
    return AuditProfileValidator(
        mutableListOf<AuditProfileRule>(
            AuditProfileCodeLengthRule(86400),
            AuditProfileExpiresAtRangeRule(64, 120),
            AuditProfileAllowedDepotRule(mutableListOf<String>("regional")),
        ),
    )
}
