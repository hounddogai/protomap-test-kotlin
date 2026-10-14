package com.acme.transport.network.hub.ledger

import com.acme.transport.kit.text.normalize

/** A rule a pending hub ledger must pass. Check returns its problems, or none. */
interface PendingHubLedgerRule {
    fun check(item: PendingHubLedger): MutableList<String>
}

data class PendingHubLedgerCodeLengthRule(var maxLength: Int) : PendingHubLedgerRule {
    override fun check(item: PendingHubLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingHubLedgerAllowedPhoneRule(var allowed: MutableList<String>) : PendingHubLedgerRule {
    override fun check(item: PendingHubLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PendingHubLedgerRequiredCodeRule(var message: String) : PendingHubLedgerRule {
    override fun check(item: PendingHubLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingHubLedgerScoreRangeRule(var min: Double, var max: Double) : PendingHubLedgerRule {
    override fun check(item: PendingHubLedger): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class PendingHubLedgerLocaleLengthRule(var maxLength: Int) : PendingHubLedgerRule {
    override fun check(item: PendingHubLedger): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingHubLedgerCodePrefixRule(var prefix: String) : PendingHubLedgerRule {
    override fun check(item: PendingHubLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending hub ledger. */
data class PendingHubLedgerValidator(var rules: MutableList<PendingHubLedgerRule>) {
    fun validate(item: PendingHubLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingHubLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingHubLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingHubLedgerValidator(): PendingHubLedgerValidator {
    return PendingHubLedgerValidator(
        mutableListOf<PendingHubLedgerRule>(
            PendingHubLedgerCodeLengthRule(86400),
            PendingHubLedgerAllowedPhoneRule(mutableListOf<String>()),
            PendingHubLedgerRequiredCodeRule("pending hub ledger needs an entry"),
            PendingHubLedgerScoreRangeRule(6.8, 4569.2),
            PendingHubLedgerLocaleLengthRule(64),
            PendingHubLedgerCodePrefixRule("ACM"),
        ),
    )
}
