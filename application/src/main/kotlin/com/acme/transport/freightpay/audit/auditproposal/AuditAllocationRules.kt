package com.acme.transport.freightpay.audit.auditproposal

/** A rule a audit allocation must pass. Check returns its problems, or none. */
interface AuditAllocationRule {
    fun check(item: AuditAllocation): MutableList<String>
}

data class AuditAllocationCodePrefixRule(var prefix: String) : AuditAllocationRule {
    override fun check(item: AuditAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AuditAllocationCodeLengthRule(var maxLength: Int) : AuditAllocationRule {
    override fun check(item: AuditAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditAllocationRequiredTitleRule(var message: String) : AuditAllocationRule {
    override fun check(item: AuditAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditAllocationRequiredLocaleRule(var message: String) : AuditAllocationRule {
    override fun check(item: AuditAllocation): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditAllocationTitleLengthRule(var maxLength: Int) : AuditAllocationRule {
    override fun check(item: AuditAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditAllocationLocaleLengthRule(var maxLength: Int) : AuditAllocationRule {
    override fun check(item: AuditAllocation): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit allocation. */
data class AuditAllocationValidator(var rules: MutableList<AuditAllocationRule>) {
    fun validate(item: AuditAllocation): MutableList<String> {
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

    fun requireValid(item: AuditAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditAllocationValidator(): AuditAllocationValidator {
    return AuditAllocationValidator(
        mutableListOf<AuditAllocationRule>(
            AuditAllocationCodePrefixRule("X"),
            AuditAllocationCodeLengthRule(64),
            AuditAllocationRequiredTitleRule("audit allocation needs a value"),
            AuditAllocationRequiredLocaleRule("audit allocation needs a value"),
            AuditAllocationTitleLengthRule(32),
            AuditAllocationLocaleLengthRule(80),
        ),
    )
}
