package com.acme.transport.safety.cameraevent.globaleventaudit

/** A rule a legacy camera event audit must pass. Check returns its problems, or none. */
interface LegacyCameraEventAuditRule {
    fun check(item: LegacyCameraEventAudit): MutableList<String>
}

data class LegacyCameraEventAuditRequiredCodeRule(var message: String) : LegacyCameraEventAuditRule {
    override fun check(item: LegacyCameraEventAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCameraEventAuditContactEmailLengthRule(var maxLength: Int) : LegacyCameraEventAuditRule {
    override fun check(item: LegacyCameraEventAudit): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCameraEventAuditCodeLengthRule(var maxLength: Int) : LegacyCameraEventAuditRule {
    override fun check(item: LegacyCameraEventAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy camera event audit. */
data class LegacyCameraEventAuditValidator(var rules: MutableList<LegacyCameraEventAuditRule>) {
    fun validate(item: LegacyCameraEventAudit): MutableList<String> {
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

    fun requireValid(item: LegacyCameraEventAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCameraEventAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCameraEventAuditValidator(): LegacyCameraEventAuditValidator {
    return LegacyCameraEventAuditValidator(
        mutableListOf<LegacyCameraEventAuditRule>(
            LegacyCameraEventAuditRequiredCodeRule("legacy camera event audit needs a value"),
            LegacyCameraEventAuditContactEmailLengthRule(255),
            LegacyCameraEventAuditCodeLengthRule(255),
        ),
    )
}
