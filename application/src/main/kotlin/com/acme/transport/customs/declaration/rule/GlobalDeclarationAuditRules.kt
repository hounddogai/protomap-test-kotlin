package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.text.normalize

/** A rule a global declaration audit must pass. Check returns its problems, or none. */
interface GlobalDeclarationAuditRule {
    fun check(item: GlobalDeclarationAudit): MutableList<String>
}

data class GlobalDeclarationAuditNameLengthRule(var maxLength: Int) : GlobalDeclarationAuditRule {
    override fun check(item: GlobalDeclarationAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationAuditCodeLengthRule(var maxLength: Int) : GlobalDeclarationAuditRule {
    override fun check(item: GlobalDeclarationAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationAuditRequiredCodeRule(var message: String) : GlobalDeclarationAuditRule {
    override fun check(item: GlobalDeclarationAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationAuditAmountRangeRule(
    var min: Double,
    var max: Double,
) : GlobalDeclarationAuditRule {
    override fun check(item: GlobalDeclarationAudit): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationAuditAllowedChecksumRule(
    var allowed: MutableList<String>,
) : GlobalDeclarationAuditRule {
    override fun check(item: GlobalDeclarationAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationAuditRequiredPhoneRule(var message: String) : GlobalDeclarationAuditRule {
    override fun check(item: GlobalDeclarationAudit): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global declaration audit. */
data class GlobalDeclarationAuditValidator(var rules: MutableList<GlobalDeclarationAuditRule>) {
    fun validate(item: GlobalDeclarationAudit): MutableList<String> {
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

    fun requireValid(item: GlobalDeclarationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDeclarationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDeclarationAuditValidator(): GlobalDeclarationAuditValidator {
    return GlobalDeclarationAuditValidator(
        mutableListOf<GlobalDeclarationAuditRule>(
            GlobalDeclarationAuditNameLengthRule(1000),
            GlobalDeclarationAuditCodeLengthRule(255),
            GlobalDeclarationAuditRequiredCodeRule("global declaration audit needs a value"),
            GlobalDeclarationAuditAmountRangeRule(1.9, 3355.3),
            GlobalDeclarationAuditAllowedChecksumRule(mutableListOf<String>()),
            GlobalDeclarationAuditRequiredPhoneRule("global declaration audit needs an entry"),
        ),
    )
}
