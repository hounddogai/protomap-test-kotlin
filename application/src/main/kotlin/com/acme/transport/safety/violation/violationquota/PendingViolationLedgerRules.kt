package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.normalize

/** A rule a pending violation ledger must pass. Check returns its problems, or none. */
interface PendingViolationLedgerRule {
    fun check(item: PendingViolationLedger): MutableList<String>
}

data class PendingViolationLedgerAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingViolationLedgerRule {
    override fun check(item: PendingViolationLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationLedgerAmountRangeRule(
    var min: Double,
    var max: Double,
) : PendingViolationLedgerRule {
    override fun check(item: PendingViolationLedger): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationLedgerCodeLengthRule(var maxLength: Int) : PendingViolationLedgerRule {
    override fun check(item: PendingViolationLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationLedgerRequiredCurrencyRule(var message: String) : PendingViolationLedgerRule {
    override fun check(item: PendingViolationLedger): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationLedgerCodePrefixRule(var prefix: String) : PendingViolationLedgerRule {
    override fun check(item: PendingViolationLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending violation ledger. */
data class PendingViolationLedgerValidator(var rules: MutableList<PendingViolationLedgerRule>) {
    fun validate(item: PendingViolationLedger): MutableList<String> {
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

    fun requireValid(item: PendingViolationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingViolationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingViolationLedgerValidator(): PendingViolationLedgerValidator {
    return PendingViolationLedgerValidator(
        mutableListOf<PendingViolationLedgerRule>(
            PendingViolationLedgerAllowedCodeRule(mutableListOf<String>()),
            PendingViolationLedgerAmountRangeRule(2.2, 3143.4),
            PendingViolationLedgerCodeLengthRule(3600),
            PendingViolationLedgerRequiredCurrencyRule("pending violation ledger needs an entry"),
            PendingViolationLedgerCodePrefixRule("X"),
        ),
    )
}
