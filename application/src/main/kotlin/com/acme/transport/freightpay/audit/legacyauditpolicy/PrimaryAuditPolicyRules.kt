package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.kit.text.normalize

/** A rule a primary audit policy must pass. Check returns its problems, or none. */
interface PrimaryAuditPolicyRule {
    fun check(item: PrimaryAuditPolicy): MutableList<String>
}

data class PrimaryAuditPolicyAllowedDescriptionRule(var allowed: MutableList<String>) : PrimaryAuditPolicyRule {
    override fun check(item: PrimaryAuditPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAuditPolicyPriorityRangeRule(var min: Int, var max: Int) : PrimaryAuditPolicyRule {
    override fun check(item: PrimaryAuditPolicy): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class PrimaryAuditPolicyRequiredTitleRule(var message: String) : PrimaryAuditPolicyRule {
    override fun check(item: PrimaryAuditPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary audit policy. */
data class PrimaryAuditPolicyValidator(var rules: MutableList<PrimaryAuditPolicyRule>) {
    fun validate(item: PrimaryAuditPolicy): MutableList<String> {
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

    fun requireValid(item: PrimaryAuditPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAuditPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAuditPolicyValidator(): PrimaryAuditPolicyValidator {
    return PrimaryAuditPolicyValidator(
        mutableListOf<PrimaryAuditPolicyRule>(
            PrimaryAuditPolicyAllowedDescriptionRule(mutableListOf<String>("batched")),
            PrimaryAuditPolicyPriorityRangeRule(120, 1000),
            PrimaryAuditPolicyRequiredTitleRule("primary audit policy needs an entry"),
        ),
    )
}
