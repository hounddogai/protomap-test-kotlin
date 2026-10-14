package com.acme.transport.lastmile.redelivery.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a redelivery ledger must pass. Check returns its problems, or none. */
interface RedeliveryLedgerRule {
    fun check(item: RedeliveryLedger): MutableList<String>
}

data class RedeliveryLedgerSummaryLengthRule(var maxLength: Int) : RedeliveryLedgerRule {
    override fun check(item: RedeliveryLedger): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryLedgerTitleLengthRule(var maxLength: Int) : RedeliveryLedgerRule {
    override fun check(item: RedeliveryLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryLedgerRequiredLabelRule(var message: String) : RedeliveryLedgerRule {
    override fun check(item: RedeliveryLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryLedgerRequiredSummaryRule(var message: String) : RedeliveryLedgerRule {
    override fun check(item: RedeliveryLedger): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryLedgerAllowedSummaryRule(var allowed: MutableList<String>) : RedeliveryLedgerRule {
    override fun check(item: RedeliveryLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryLedgerCodeLengthRule(var maxLength: Int) : RedeliveryLedgerRule {
    override fun check(item: RedeliveryLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery ledger. */
data class RedeliveryLedgerValidator(var rules: MutableList<RedeliveryLedgerRule>) {
    fun validate(item: RedeliveryLedger): MutableList<String> {
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

    fun requireValid(item: RedeliveryLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryLedgerValidator(): RedeliveryLedgerValidator {
    return RedeliveryLedgerValidator(
        mutableListOf<RedeliveryLedgerRule>(
            RedeliveryLedgerSummaryLengthRule(120),
            RedeliveryLedgerTitleLengthRule(80),
            RedeliveryLedgerRequiredLabelRule("redelivery ledger needs a value"),
            RedeliveryLedgerRequiredSummaryRule("redelivery ledger needs an entry"),
            RedeliveryLedgerAllowedSummaryRule(mutableListOf<String>()),
            RedeliveryLedgerCodeLengthRule(64),
        ),
    )
}
