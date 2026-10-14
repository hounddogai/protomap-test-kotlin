package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.text.normalize

/** A rule a lease return claim must pass. Check returns its problems, or none. */
interface LeaseReturnClaimRule {
    fun check(item: LeaseReturnClaim): MutableList<String>
}

data class LeaseReturnClaimRequiredTitleRule(var message: String) : LeaseReturnClaimRule {
    override fun check(item: LeaseReturnClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnClaimCodeLengthRule(var maxLength: Int) : LeaseReturnClaimRule {
    override fun check(item: LeaseReturnClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnClaimAllowedRegionRule(var allowed: MutableList<String>) : LeaseReturnClaimRule {
    override fun check(item: LeaseReturnClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return claim. */
data class LeaseReturnClaimValidator(var rules: MutableList<LeaseReturnClaimRule>) {
    fun validate(item: LeaseReturnClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnClaimValidator(): LeaseReturnClaimValidator {
    return LeaseReturnClaimValidator(
        mutableListOf<LeaseReturnClaimRule>(
            LeaseReturnClaimRequiredTitleRule("lease return claim needs a value"),
            LeaseReturnClaimCodeLengthRule(80),
            LeaseReturnClaimAllowedRegionRule(mutableListOf<String>("seasonal", "adaptive", "fallback")),
        ),
    )
}
