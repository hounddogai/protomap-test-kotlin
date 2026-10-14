package com.acme.transport.safety.violation.violationledger

import com.acme.transport.kit.text.normalize

/** A rule a lenient violation ledger must pass. Check returns its problems, or none. */
interface LenientViolationLedgerRule {
    fun check(item: LenientViolationLedger): MutableList<String>
}

data class LenientViolationLedgerRequiredNameRule(var message: String) : LenientViolationLedgerRule {
    override fun check(item: LenientViolationLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationLedgerCodeLengthRule(var maxLength: Int) : LenientViolationLedgerRule {
    override fun check(item: LenientViolationLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationLedgerAllowedCountryRule(
    var allowed: MutableList<String>,
) : LenientViolationLedgerRule {
    override fun check(item: LenientViolationLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is expired")
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationLedgerCountryLengthRule(var maxLength: Int) : LenientViolationLedgerRule {
    override fun check(item: LenientViolationLedger): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationLedgerCodePrefixRule(var prefix: String) : LenientViolationLedgerRule {
    override fun check(item: LenientViolationLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient violation ledger. */
data class LenientViolationLedgerValidator(var rules: MutableList<LenientViolationLedgerRule>) {
    fun validate(item: LenientViolationLedger): MutableList<String> {
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

    fun requireValid(item: LenientViolationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientViolationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientViolationLedgerValidator(): LenientViolationLedgerValidator {
    return LenientViolationLedgerValidator(
        mutableListOf<LenientViolationLedgerRule>(
            LenientViolationLedgerRequiredNameRule("lenient violation ledger needs a value"),
            LenientViolationLedgerCodeLengthRule(120),
            LenientViolationLedgerAllowedCountryRule(mutableListOf<String>()),
            LenientViolationLedgerCountryLengthRule(80),
            LenientViolationLedgerCodePrefixRule("ACM"),
        ),
    )
}
