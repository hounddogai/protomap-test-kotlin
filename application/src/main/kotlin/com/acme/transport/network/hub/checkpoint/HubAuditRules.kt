package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a hub audit must pass. Check returns its problems, or none. */
interface HubAuditRule {
    fun check(item: HubAudit): MutableList<String>
}

data class HubAuditRequiredNameRule(var message: String) : HubAuditRule {
    override fun check(item: HubAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubAuditAllowedCodeRule(var allowed: MutableList<String>) : HubAuditRule {
    override fun check(item: HubAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class HubAuditNameLengthRule(var maxLength: Int) : HubAuditRule {
    override fun check(item: HubAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubAuditTimezoneLengthRule(var maxLength: Int) : HubAuditRule {
    override fun check(item: HubAudit): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubAuditCodePrefixRule(var prefix: String) : HubAuditRule {
    override fun check(item: HubAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubAuditCreatedAtRangeRule(var min: Int, var max: Int) : HubAuditRule {
    override fun check(item: HubAudit): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a hub audit. */
data class HubAuditValidator(var rules: MutableList<HubAuditRule>) {
    fun validate(item: HubAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultHubAuditValidator(): HubAuditValidator {
    return HubAuditValidator(
        mutableListOf<HubAuditRule>(
            HubAuditRequiredNameRule("hub audit needs an entry"),
            HubAuditAllowedCodeRule(mutableListOf<String>("manual", "capped")),
            HubAuditNameLengthRule(32),
            HubAuditTimezoneLengthRule(86400),
            HubAuditCodePrefixRule("Q"),
            HubAuditCreatedAtRangeRule(64, 80),
        ),
    )
}
