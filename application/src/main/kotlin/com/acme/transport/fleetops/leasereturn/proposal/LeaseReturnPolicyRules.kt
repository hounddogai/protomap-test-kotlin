package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.text.normalize

/** A rule a lease return policy must pass. Check returns its problems, or none. */
interface LeaseReturnPolicyRule {
    fun check(item: LeaseReturnPolicy): MutableList<String>
}

data class LeaseReturnPolicyRequiredExternalRefRule(var message: String) : LeaseReturnPolicyRule {
    override fun check(item: LeaseReturnPolicy): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnPolicyExternalRefLengthRule(var maxLength: Int) : LeaseReturnPolicyRule {
    override fun check(item: LeaseReturnPolicy): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnPolicyAllowedDescriptionRule(var allowed: MutableList<String>) : LeaseReturnPolicyRule {
    override fun check(item: LeaseReturnPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return policy. */
data class LeaseReturnPolicyValidator(var rules: MutableList<LeaseReturnPolicyRule>) {
    fun validate(item: LeaseReturnPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnPolicyValidator(): LeaseReturnPolicyValidator {
    return LeaseReturnPolicyValidator(
        mutableListOf<LeaseReturnPolicyRule>(
            LeaseReturnPolicyRequiredExternalRefRule("lease return policy needs a value"),
            LeaseReturnPolicyExternalRefLengthRule(255),
            LeaseReturnPolicyAllowedDescriptionRule(mutableListOf<String>("tiered", "adaptive")),
        ),
    )
}
