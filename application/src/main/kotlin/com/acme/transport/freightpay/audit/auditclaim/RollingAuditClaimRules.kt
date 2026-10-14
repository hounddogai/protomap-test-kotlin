package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.text.normalize

/** A rule a rolling audit claim must pass. Check returns its problems, or none. */
interface RollingAuditClaimRule {
    fun check(item: RollingAuditClaim): MutableList<String>
}

data class RollingAuditClaimNameLengthRule(var maxLength: Int) : RollingAuditClaimRule {
    override fun check(item: RollingAuditClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditClaimRequiredCodeRule(var message: String) : RollingAuditClaimRule {
    override fun check(item: RollingAuditClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditClaimAllowedLaneRule(var allowed: MutableList<String>) : RollingAuditClaimRule {
    override fun check(item: RollingAuditClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling audit claim. */
data class RollingAuditClaimValidator(var rules: MutableList<RollingAuditClaimRule>) {
    fun validate(item: RollingAuditClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingAuditClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAuditClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAuditClaimValidator(): RollingAuditClaimValidator {
    return RollingAuditClaimValidator(
        mutableListOf<RollingAuditClaimRule>(
            RollingAuditClaimNameLengthRule(64),
            RollingAuditClaimRequiredCodeRule("rolling audit claim needs this field"),
            RollingAuditClaimAllowedLaneRule(mutableListOf<String>("regional", "default", "adaptive")),
        ),
    )
}
