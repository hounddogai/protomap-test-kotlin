package com.acme.transport.freightpay.audit.policy

import com.acme.transport.kit.text.normalize

/** A rule a audit override must pass. Check returns its problems, or none. */
interface AuditOverrideRule {
    fun check(item: AuditOverride): MutableList<String>
}

data class AuditOverrideTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : AuditOverrideRule {
    override fun check(item: AuditOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class AuditOverrideCountryLengthRule(var maxLength: Int) : AuditOverrideRule {
    override fun check(item: AuditOverride): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditOverrideAllowedCodeRule(var allowed: MutableList<String>) : AuditOverrideRule {
    override fun check(item: AuditOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AuditOverrideRequiredCountryRule(var message: String) : AuditOverrideRule {
    override fun check(item: AuditOverride): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditOverrideCodeLengthRule(var maxLength: Int) : AuditOverrideRule {
    override fun check(item: AuditOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit override. */
data class AuditOverrideValidator(var rules: MutableList<AuditOverrideRule>) {
    fun validate(item: AuditOverride): MutableList<String> {
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

    fun requireValid(item: AuditOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditOverrideValidator(): AuditOverrideValidator {
    return AuditOverrideValidator(
        mutableListOf<AuditOverrideRule>(
            AuditOverrideTagsLimitRule(32, mutableListOf<String>("capped", "batched")),
            AuditOverrideCountryLengthRule(16),
            AuditOverrideAllowedCodeRule(mutableListOf<String>()),
            AuditOverrideRequiredCountryRule("audit override needs a value"),
            AuditOverrideCodeLengthRule(80),
        ),
    )
}
