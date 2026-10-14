package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.kit.text.normalize

/** A rule a strict audit claim must pass. Check returns its problems, or none. */
interface StrictAuditClaimRule {
    fun check(item: StrictAuditClaim): MutableList<String>
}

data class StrictAuditClaimApprovedGuardRule(var limit: Double) : StrictAuditClaimRule {
    override fun check(item: StrictAuditClaim): MutableList<String> {
        if (item.approved && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class StrictAuditClaimCodePrefixRule(var prefix: String) : StrictAuditClaimRule {
    override fun check(item: StrictAuditClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictAuditClaimAllowedRegionRule(var allowed: MutableList<String>) : StrictAuditClaimRule {
    override fun check(item: StrictAuditClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict audit claim. */
data class StrictAuditClaimValidator(var rules: MutableList<StrictAuditClaimRule>) {
    fun validate(item: StrictAuditClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictAuditClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAuditClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAuditClaimValidator(): StrictAuditClaimValidator {
    return StrictAuditClaimValidator(
        mutableListOf<StrictAuditClaimRule>(
            StrictAuditClaimApprovedGuardRule(2.1),
            StrictAuditClaimCodePrefixRule("Q"),
            StrictAuditClaimAllowedRegionRule(mutableListOf<String>("fallback", "manual", "tiered")),
        ),
    )
}
