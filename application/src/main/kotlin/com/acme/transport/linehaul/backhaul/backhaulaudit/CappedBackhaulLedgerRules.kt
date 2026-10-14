package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.text.normalize

/** A rule a capped backhaul ledger must pass. Check returns its problems, or none. */
interface CappedBackhaulLedgerRule {
    fun check(item: CappedBackhaulLedger): MutableList<String>
}

data class CappedBackhaulLedgerRequiredCodeRule(var message: String) : CappedBackhaulLedgerRule {
    override fun check(item: CappedBackhaulLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulLedgerRequiredTitleRule(var message: String) : CappedBackhaulLedgerRule {
    override fun check(item: CappedBackhaulLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulLedgerAllowedPhoneRule(var allowed: MutableList<String>) : CappedBackhaulLedgerRule {
    override fun check(item: CappedBackhaulLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped backhaul ledger. */
data class CappedBackhaulLedgerValidator(var rules: MutableList<CappedBackhaulLedgerRule>) {
    fun validate(item: CappedBackhaulLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedBackhaulLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBackhaulLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBackhaulLedgerValidator(): CappedBackhaulLedgerValidator {
    return CappedBackhaulLedgerValidator(
        mutableListOf<CappedBackhaulLedgerRule>(
            CappedBackhaulLedgerRequiredCodeRule("capped backhaul ledger needs an entry"),
            CappedBackhaulLedgerRequiredTitleRule("capped backhaul ledger needs an entry"),
            CappedBackhaulLedgerAllowedPhoneRule(mutableListOf<String>()),
        ),
    )
}
