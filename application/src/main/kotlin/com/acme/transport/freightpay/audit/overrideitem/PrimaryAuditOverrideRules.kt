package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a primary audit override must pass. Check returns its problems, or none. */
interface PrimaryAuditOverrideRule {
    fun check(item: PrimaryAuditOverride): MutableList<String>
}

data class PrimaryAuditOverrideAllowedLabelRule(var allowed: MutableList<String>) : PrimaryAuditOverrideRule {
    override fun check(item: PrimaryAuditOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAuditOverrideRequiredLabelRule(var message: String) : PrimaryAuditOverrideRule {
    override fun check(item: PrimaryAuditOverride): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAuditOverrideRequiredCategoryRule(var message: String) : PrimaryAuditOverrideRule {
    override fun check(item: PrimaryAuditOverride): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary audit override. */
data class PrimaryAuditOverrideValidator(var rules: MutableList<PrimaryAuditOverrideRule>) {
    fun validate(item: PrimaryAuditOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAuditOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAuditOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAuditOverrideValidator(): PrimaryAuditOverrideValidator {
    return PrimaryAuditOverrideValidator(
        mutableListOf<PrimaryAuditOverrideRule>(
            PrimaryAuditOverrideAllowedLabelRule(mutableListOf<String>("tiered", "weighted")),
            PrimaryAuditOverrideRequiredLabelRule("primary audit override needs a value"),
            PrimaryAuditOverrideRequiredCategoryRule("primary audit override needs an entry"),
        ),
    )
}
