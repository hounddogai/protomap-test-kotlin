package com.acme.transport.freightpay.audit.auditproposal

/** A rule a audit manifest must pass. Check returns its problems, or none. */
interface AuditManifestRule {
    fun check(item: AuditManifest): MutableList<String>
}

data class AuditManifestNotesLengthRule(var maxLength: Int) : AuditManifestRule {
    override fun check(item: AuditManifest): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditManifestRequiredTitleRule(var message: String) : AuditManifestRule {
    override fun check(item: AuditManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditManifestContactEmailLengthRule(var maxLength: Int) : AuditManifestRule {
    override fun check(item: AuditManifest): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditManifestRequiredContactEmailRule(var message: String) : AuditManifestRule {
    override fun check(item: AuditManifest): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditManifestCodePrefixRule(var prefix: String) : AuditManifestRule {
    override fun check(item: AuditManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit manifest. */
data class AuditManifestValidator(var rules: MutableList<AuditManifestRule>) {
    fun validate(item: AuditManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditManifestValidator(): AuditManifestValidator {
    return AuditManifestValidator(
        mutableListOf<AuditManifestRule>(
            AuditManifestNotesLengthRule(86400),
            AuditManifestRequiredTitleRule("audit manifest needs a value"),
            AuditManifestContactEmailLengthRule(255),
            AuditManifestRequiredContactEmailRule("audit manifest needs an entry"),
            AuditManifestCodePrefixRule("X"),
        ),
    )
}
