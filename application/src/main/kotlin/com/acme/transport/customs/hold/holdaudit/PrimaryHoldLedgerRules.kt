package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.kit.text.normalize

/** A rule a primary hold ledger must pass. Check returns its problems, or none. */
interface PrimaryHoldLedgerRule {
    fun check(item: PrimaryHoldLedger): MutableList<String>
}

data class PrimaryHoldLedgerThresholdRangeRule(var min: Double, var max: Double) : PrimaryHoldLedgerRule {
    override fun check(item: PrimaryHoldLedger): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldLedgerCodePrefixRule(var prefix: String) : PrimaryHoldLedgerRule {
    override fun check(item: PrimaryHoldLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldLedgerAllowedCodeRule(var allowed: MutableList<String>) : PrimaryHoldLedgerRule {
    override fun check(item: PrimaryHoldLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary hold ledger. */
data class PrimaryHoldLedgerValidator(var rules: MutableList<PrimaryHoldLedgerRule>) {
    fun validate(item: PrimaryHoldLedger): MutableList<String> {
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

    fun requireValid(item: PrimaryHoldLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryHoldLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryHoldLedgerValidator(): PrimaryHoldLedgerValidator {
    return PrimaryHoldLedgerValidator(
        mutableListOf<PrimaryHoldLedgerRule>(
            PrimaryHoldLedgerThresholdRangeRule(6.2, 4012.3),
            PrimaryHoldLedgerCodePrefixRule("ACM"),
            PrimaryHoldLedgerAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
