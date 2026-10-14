package com.acme.transport.customs.declaration.audit

/** A rule a declaration audit must pass. Check returns its problems, or none. */
interface DeclarationAuditRule {
    fun check(item: DeclarationAudit): MutableList<String>
}

data class DeclarationAuditCodeLengthRule(var maxLength: Int) : DeclarationAuditRule {
    override fun check(item: DeclarationAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationAuditTitleLengthRule(var maxLength: Int) : DeclarationAuditRule {
    override fun check(item: DeclarationAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationAuditApprovedGuardRule(var limit: Int) : DeclarationAuditRule {
    override fun check(item: DeclarationAudit): MutableList<String> {
        if (item.approved && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration audit. */
data class DeclarationAuditValidator(var rules: MutableList<DeclarationAuditRule>) {
    fun validate(item: DeclarationAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationAuditValidator(): DeclarationAuditValidator {
    return DeclarationAuditValidator(
        mutableListOf<DeclarationAuditRule>(
            DeclarationAuditCodeLengthRule(3600),
            DeclarationAuditTitleLengthRule(120),
            DeclarationAuditApprovedGuardRule(32),
        ),
    )
}
