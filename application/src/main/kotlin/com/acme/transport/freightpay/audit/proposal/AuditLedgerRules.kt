package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.text.normalize

/** A rule a audit ledger must pass. Check returns its problems, or none. */
interface AuditLedgerRule {
    fun check(item: AuditLedger): MutableList<String>
}

data class AuditLedgerAllowedTrailerIdRule(var allowed: MutableList<String>) : AuditLedgerRule {
    override fun check(item: AuditLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AuditLedgerRequiredNameRule(var message: String) : AuditLedgerRule {
    override fun check(item: AuditLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditLedgerCodeLengthRule(var maxLength: Int) : AuditLedgerRule {
    override fun check(item: AuditLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditLedgerRequiredCategoryRule(var message: String) : AuditLedgerRule {
    override fun check(item: AuditLedger): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditLedgerMaximumRangeRule(var min: Double, var max: Double) : AuditLedgerRule {
    override fun check(item: AuditLedger): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditLedgerTrailerIdLengthRule(var maxLength: Int) : AuditLedgerRule {
    override fun check(item: AuditLedger): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit ledger. */
data class AuditLedgerValidator(var rules: MutableList<AuditLedgerRule>) {
    fun validate(item: AuditLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditLedgerValidator(): AuditLedgerValidator {
    return AuditLedgerValidator(
        mutableListOf<AuditLedgerRule>(
            AuditLedgerAllowedTrailerIdRule(mutableListOf<String>("scheduled")),
            AuditLedgerRequiredNameRule("audit ledger needs a value"),
            AuditLedgerCodeLengthRule(120),
            AuditLedgerRequiredCategoryRule("audit ledger needs an entry"),
            AuditLedgerMaximumRangeRule(5.0, 3558.4),
            AuditLedgerTrailerIdLengthRule(64),
        ),
    )
}
