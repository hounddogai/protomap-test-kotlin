package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.text.normalize

/** A rule a adaptive audit record must pass. Check returns its problems, or none. */
interface AdaptiveAuditRecordRule {
    fun check(item: AdaptiveAuditRecord): MutableList<String>
}

data class AdaptiveAuditRecordRequiredLabelRule(var message: String) : AdaptiveAuditRecordRule {
    override fun check(item: AdaptiveAuditRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditRecordCodeLengthRule(var maxLength: Int) : AdaptiveAuditRecordRule {
    override fun check(item: AdaptiveAuditRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditRecordAllowedLabelRule(var allowed: MutableList<String>) : AdaptiveAuditRecordRule {
    override fun check(item: AdaptiveAuditRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditRecordRequiredCodeRule(var message: String) : AdaptiveAuditRecordRule {
    override fun check(item: AdaptiveAuditRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive audit record. */
data class AdaptiveAuditRecordValidator(var rules: MutableList<AdaptiveAuditRecordRule>) {
    fun validate(item: AdaptiveAuditRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAuditRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAuditRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAuditRecordValidator(): AdaptiveAuditRecordValidator {
    return AdaptiveAuditRecordValidator(
        mutableListOf<AdaptiveAuditRecordRule>(
            AdaptiveAuditRecordRequiredLabelRule("adaptive audit record needs a value"),
            AdaptiveAuditRecordCodeLengthRule(1000),
            AdaptiveAuditRecordAllowedLabelRule(mutableListOf<String>("lenient", "adaptive")),
            AdaptiveAuditRecordRequiredCodeRule("adaptive audit record needs a value"),
        ),
    )
}
