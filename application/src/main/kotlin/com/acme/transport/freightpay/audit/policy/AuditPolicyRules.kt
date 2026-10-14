package com.acme.transport.freightpay.audit.policy

import com.acme.transport.kit.text.normalize

/** A rule a audit policy must pass. Check returns its problems, or none. */
interface AuditPolicyRule {
    fun check(item: AuditPolicy): MutableList<String>
}

data class AuditPolicyTitleLengthRule(var maxLength: Int) : AuditPolicyRule {
    override fun check(item: AuditPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditPolicyAllowedTitleRule(var allowed: MutableList<String>) : AuditPolicyRule {
    override fun check(item: AuditPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class AuditPolicyCurrencyLengthRule(var maxLength: Int) : AuditPolicyRule {
    override fun check(item: AuditPolicy): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditPolicyRequiredTitleRule(var message: String) : AuditPolicyRule {
    override fun check(item: AuditPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit policy. */
data class AuditPolicyValidator(var rules: MutableList<AuditPolicyRule>) {
    fun validate(item: AuditPolicy): MutableList<String> {
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

    fun requireValid(item: AuditPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditPolicyValidator(): AuditPolicyValidator {
    return AuditPolicyValidator(
        mutableListOf<AuditPolicyRule>(
            AuditPolicyTitleLengthRule(86400),
            AuditPolicyAllowedTitleRule(mutableListOf<String>()),
            AuditPolicyCurrencyLengthRule(86400),
            AuditPolicyRequiredTitleRule("audit policy needs an entry"),
        ),
    )
}
