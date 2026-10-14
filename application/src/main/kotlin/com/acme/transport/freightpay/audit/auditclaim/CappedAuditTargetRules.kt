package com.acme.transport.freightpay.audit.auditclaim

/** A rule a capped audit target must pass. Check returns its problems, or none. */
interface CappedAuditTargetRule {
    fun check(item: CappedAuditTarget): MutableList<String>
}

data class CappedAuditTargetNameLengthRule(var maxLength: Int) : CappedAuditTargetRule {
    override fun check(item: CappedAuditTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAuditTargetCodePrefixRule(var prefix: String) : CappedAuditTargetRule {
    override fun check(item: CappedAuditTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedAuditTargetRequiredCurrencyRule(var message: String) : CappedAuditTargetRule {
    override fun check(item: CappedAuditTarget): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped audit target. */
data class CappedAuditTargetValidator(var rules: MutableList<CappedAuditTargetRule>) {
    fun validate(item: CappedAuditTarget): MutableList<String> {
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

    fun requireValid(item: CappedAuditTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAuditTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAuditTargetValidator(): CappedAuditTargetValidator {
    return CappedAuditTargetValidator(
        mutableListOf<CappedAuditTargetRule>(
            CappedAuditTargetNameLengthRule(16),
            CappedAuditTargetCodePrefixRule("REF"),
            CappedAuditTargetRequiredCurrencyRule("capped audit target needs this field"),
        ),
    )
}
