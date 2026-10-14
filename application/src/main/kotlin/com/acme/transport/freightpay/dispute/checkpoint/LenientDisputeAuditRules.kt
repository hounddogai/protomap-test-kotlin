package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a lenient dispute audit must pass. Check returns its problems, or none. */
interface LenientDisputeAuditRule {
    fun check(item: LenientDisputeAudit): MutableList<String>
}

data class LenientDisputeAuditTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LenientDisputeAuditRule {
    override fun check(item: LenientDisputeAudit): MutableList<String> {
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

data class LenientDisputeAuditAllowedTimezoneRule(var allowed: MutableList<String>) : LenientDisputeAuditRule {
    override fun check(item: LenientDisputeAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeAuditRequiredTimezoneRule(var message: String) : LenientDisputeAuditRule {
    override fun check(item: LenientDisputeAudit): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeAuditTitleLengthRule(var maxLength: Int) : LenientDisputeAuditRule {
    override fun check(item: LenientDisputeAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeAuditCodePrefixRule(var prefix: String) : LenientDisputeAuditRule {
    override fun check(item: LenientDisputeAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeAuditCodeLengthRule(var maxLength: Int) : LenientDisputeAuditRule {
    override fun check(item: LenientDisputeAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dispute audit. */
data class LenientDisputeAuditValidator(var rules: MutableList<LenientDisputeAuditRule>) {
    fun validate(item: LenientDisputeAudit): MutableList<String> {
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

    fun requireValid(item: LenientDisputeAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDisputeAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDisputeAuditValidator(): LenientDisputeAuditValidator {
    return LenientDisputeAuditValidator(
        mutableListOf<LenientDisputeAuditRule>(
            LenientDisputeAuditTagsLimitRule(86400, mutableListOf<String>()),
            LenientDisputeAuditAllowedTimezoneRule(mutableListOf<String>("default", "capped")),
            LenientDisputeAuditRequiredTimezoneRule("lenient dispute audit needs an entry"),
            LenientDisputeAuditTitleLengthRule(1000),
            LenientDisputeAuditCodePrefixRule("X"),
            LenientDisputeAuditCodeLengthRule(86400),
        ),
    )
}
