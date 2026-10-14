package com.acme.transport.freightpay.audit.auditrule

/** A rule a lenient audit record must pass. Check returns its problems, or none. */
interface LenientAuditRecordRule {
    fun check(item: LenientAuditRecord): MutableList<String>
}

data class LenientAuditRecordContactEmailLengthRule(var maxLength: Int) : LenientAuditRecordRule {
    override fun check(item: LenientAuditRecord): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAuditRecordTitleLengthRule(var maxLength: Int) : LenientAuditRecordRule {
    override fun check(item: LenientAuditRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAuditRecordRequiredCodeRule(var message: String) : LenientAuditRecordRule {
    override fun check(item: LenientAuditRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAuditRecordCodeLengthRule(var maxLength: Int) : LenientAuditRecordRule {
    override fun check(item: LenientAuditRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient audit record. */
data class LenientAuditRecordValidator(var rules: MutableList<LenientAuditRecordRule>) {
    fun validate(item: LenientAuditRecord): MutableList<String> {
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

    fun requireValid(item: LenientAuditRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAuditRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAuditRecordValidator(): LenientAuditRecordValidator {
    return LenientAuditRecordValidator(
        mutableListOf<LenientAuditRecordRule>(
            LenientAuditRecordContactEmailLengthRule(255),
            LenientAuditRecordTitleLengthRule(255),
            LenientAuditRecordRequiredCodeRule("lenient audit record needs an entry"),
            LenientAuditRecordCodeLengthRule(255),
        ),
    )
}
