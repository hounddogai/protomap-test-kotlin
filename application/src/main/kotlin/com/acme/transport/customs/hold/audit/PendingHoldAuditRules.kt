package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.text.normalize

/** A rule a pending hold audit must pass. Check returns its problems, or none. */
interface PendingHoldAuditRule {
    fun check(item: PendingHoldAudit): MutableList<String>
}

data class PendingHoldAuditTitleLengthRule(var maxLength: Int) : PendingHoldAuditRule {
    override fun check(item: PendingHoldAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldAuditOwnerIdLengthRule(var maxLength: Int) : PendingHoldAuditRule {
    override fun check(item: PendingHoldAudit): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldAuditRequiredCodeRule(var message: String) : PendingHoldAuditRule {
    override fun check(item: PendingHoldAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldAuditAllowedCodeRule(var allowed: MutableList<String>) : PendingHoldAuditRule {
    override fun check(item: PendingHoldAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldAuditCodeLengthRule(var maxLength: Int) : PendingHoldAuditRule {
    override fun check(item: PendingHoldAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending hold audit. */
data class PendingHoldAuditValidator(var rules: MutableList<PendingHoldAuditRule>) {
    fun validate(item: PendingHoldAudit): MutableList<String> {
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

    fun requireValid(item: PendingHoldAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingHoldAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingHoldAuditValidator(): PendingHoldAuditValidator {
    return PendingHoldAuditValidator(
        mutableListOf<PendingHoldAuditRule>(
            PendingHoldAuditTitleLengthRule(255),
            PendingHoldAuditOwnerIdLengthRule(16),
            PendingHoldAuditRequiredCodeRule("pending hold audit needs a value"),
            PendingHoldAuditAllowedCodeRule(mutableListOf<String>("weighted")),
            PendingHoldAuditCodeLengthRule(64),
        ),
    )
}
