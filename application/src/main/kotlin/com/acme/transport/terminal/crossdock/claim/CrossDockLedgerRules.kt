package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.text.normalize

/** A rule a cross dock ledger must pass. Check returns its problems, or none. */
interface CrossDockLedgerRule {
    fun check(item: CrossDockLedger): MutableList<String>
}

data class CrossDockLedgerAllowedSourceRule(var allowed: MutableList<String>) : CrossDockLedgerRule {
    override fun check(item: CrossDockLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is expired")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockLedgerTitleLengthRule(var maxLength: Int) : CrossDockLedgerRule {
    override fun check(item: CrossDockLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockLedgerRequiredCodeRule(var message: String) : CrossDockLedgerRule {
    override fun check(item: CrossDockLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockLedgerCurrencyLengthRule(var maxLength: Int) : CrossDockLedgerRule {
    override fun check(item: CrossDockLedger): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock ledger. */
data class CrossDockLedgerValidator(var rules: MutableList<CrossDockLedgerRule>) {
    fun validate(item: CrossDockLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockLedgerValidator(): CrossDockLedgerValidator {
    return CrossDockLedgerValidator(
        mutableListOf<CrossDockLedgerRule>(
            CrossDockLedgerAllowedSourceRule(mutableListOf<String>("primary", "default", "weighted")),
            CrossDockLedgerTitleLengthRule(80),
            CrossDockLedgerRequiredCodeRule("cross dock ledger needs an entry"),
            CrossDockLedgerCurrencyLengthRule(255),
        ),
    )
}
