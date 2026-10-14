package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.text.normalize

/** A rule a audit adjustment must pass. Check returns its problems, or none. */
interface AuditAdjustmentRule {
    fun check(item: AuditAdjustment): MutableList<String>
}

data class AuditAdjustmentRequiredCountryRule(var message: String) : AuditAdjustmentRule {
    override fun check(item: AuditAdjustment): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditAdjustmentCodePrefixRule(var prefix: String) : AuditAdjustmentRule {
    override fun check(item: AuditAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AuditAdjustmentRequiredNameRule(var message: String) : AuditAdjustmentRule {
    override fun check(item: AuditAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditAdjustmentAllowedCodeRule(var allowed: MutableList<String>) : AuditAdjustmentRule {
    override fun check(item: AuditAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AuditAdjustmentNameLengthRule(var maxLength: Int) : AuditAdjustmentRule {
    override fun check(item: AuditAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit adjustment. */
data class AuditAdjustmentValidator(var rules: MutableList<AuditAdjustmentRule>) {
    fun validate(item: AuditAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditAdjustmentValidator(): AuditAdjustmentValidator {
    return AuditAdjustmentValidator(
        mutableListOf<AuditAdjustmentRule>(
            AuditAdjustmentRequiredCountryRule("audit adjustment needs this field"),
            AuditAdjustmentCodePrefixRule("REF"),
            AuditAdjustmentRequiredNameRule("audit adjustment needs a value"),
            AuditAdjustmentAllowedCodeRule(mutableListOf<String>("tiered", "primary", "lenient")),
            AuditAdjustmentNameLengthRule(1000),
        ),
    )
}
