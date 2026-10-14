package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.text.normalize

/** A rule a audit claim must pass. Check returns its problems, or none. */
interface AuditClaimRule {
    fun check(item: AuditClaim): MutableList<String>
}

data class AuditClaimAllowedCodeRule(var allowed: MutableList<String>) : AuditClaimRule {
    override fun check(item: AuditClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AuditClaimRequiredContactEmailRule(var message: String) : AuditClaimRule {
    override fun check(item: AuditClaim): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditClaimRequiredCodeRule(var message: String) : AuditClaimRule {
    override fun check(item: AuditClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit claim. */
data class AuditClaimValidator(var rules: MutableList<AuditClaimRule>) {
    fun validate(item: AuditClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditClaimValidator(): AuditClaimValidator {
    return AuditClaimValidator(
        mutableListOf<AuditClaimRule>(
            AuditClaimAllowedCodeRule(mutableListOf<String>("fallback", "lenient")),
            AuditClaimRequiredContactEmailRule("audit claim needs an entry"),
            AuditClaimRequiredCodeRule("audit claim needs this field"),
        ),
    )
}
