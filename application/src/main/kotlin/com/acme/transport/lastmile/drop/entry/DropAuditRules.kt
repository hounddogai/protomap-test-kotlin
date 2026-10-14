package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.text.normalize

/** A rule a drop audit must pass. Check returns its problems, or none. */
interface DropAuditRule {
    fun check(item: DropAudit): MutableList<String>
}

data class DropAuditCodeLengthRule(var maxLength: Int) : DropAuditRule {
    override fun check(item: DropAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropAuditAllowedCodeRule(var allowed: MutableList<String>) : DropAuditRule {
    override fun check(item: DropAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class DropAuditRequiredNameRule(var message: String) : DropAuditRule {
    override fun check(item: DropAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropAuditRequiredNotesRule(var message: String) : DropAuditRule {
    override fun check(item: DropAudit): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropAuditNotesLengthRule(var maxLength: Int) : DropAuditRule {
    override fun check(item: DropAudit): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop audit. */
data class DropAuditValidator(var rules: MutableList<DropAuditRule>) {
    fun validate(item: DropAudit): MutableList<String> {
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

    fun requireValid(item: DropAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDropAuditValidator(): DropAuditValidator {
    return DropAuditValidator(
        mutableListOf<DropAuditRule>(
            DropAuditCodeLengthRule(1000),
            DropAuditAllowedCodeRule(mutableListOf<String>("weighted")),
            DropAuditRequiredNameRule("drop audit needs an entry"),
            DropAuditRequiredNotesRule("drop audit needs an entry"),
            DropAuditNotesLengthRule(255),
        ),
    )
}
