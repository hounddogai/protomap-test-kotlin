package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.text.normalize

/** A rule a regional declaration audit must pass. Check returns its problems, or none. */
interface RegionalDeclarationAuditRule {
    fun check(item: RegionalDeclarationAudit): MutableList<String>
}

data class RegionalDeclarationAuditRequiredCodeRule(var message: String) : RegionalDeclarationAuditRule {
    override fun check(item: RegionalDeclarationAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationAuditAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : RegionalDeclarationAuditRule {
    override fun check(item: RegionalDeclarationAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationAuditLabelLengthRule(var maxLength: Int) : RegionalDeclarationAuditRule {
    override fun check(item: RegionalDeclarationAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationAuditCodePrefixRule(var prefix: String) : RegionalDeclarationAuditRule {
    override fun check(item: RegionalDeclarationAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional declaration audit. */
data class RegionalDeclarationAuditValidator(var rules: MutableList<RegionalDeclarationAuditRule>) {
    fun validate(item: RegionalDeclarationAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDeclarationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDeclarationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDeclarationAuditValidator(): RegionalDeclarationAuditValidator {
    return RegionalDeclarationAuditValidator(
        mutableListOf<RegionalDeclarationAuditRule>(
            RegionalDeclarationAuditRequiredCodeRule("regional declaration audit needs a value"),
            RegionalDeclarationAuditAllowedContactEmailRule(mutableListOf<String>()),
            RegionalDeclarationAuditLabelLengthRule(255),
            RegionalDeclarationAuditCodePrefixRule("X"),
        ),
    )
}
