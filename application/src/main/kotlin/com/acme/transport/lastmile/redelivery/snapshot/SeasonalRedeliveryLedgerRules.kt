package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a seasonal redelivery ledger must pass. Check returns its problems, or none. */
interface SeasonalRedeliveryLedgerRule {
    fun check(item: SeasonalRedeliveryLedger): MutableList<String>
}

data class SeasonalRedeliveryLedgerAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalRedeliveryLedgerRule {
    override fun check(item: SeasonalRedeliveryLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryLedgerRevisionRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalRedeliveryLedgerRule {
    override fun check(item: SeasonalRedeliveryLedger): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class SeasonalRedeliveryLedgerCodePrefixRule(var prefix: String) : SeasonalRedeliveryLedgerRule {
    override fun check(item: SeasonalRedeliveryLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryLedgerCodeLengthRule(var maxLength: Int) : SeasonalRedeliveryLedgerRule {
    override fun check(item: SeasonalRedeliveryLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal redelivery ledger. */
data class SeasonalRedeliveryLedgerValidator(var rules: MutableList<SeasonalRedeliveryLedgerRule>) {
    fun validate(item: SeasonalRedeliveryLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRedeliveryLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRedeliveryLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRedeliveryLedgerValidator(): SeasonalRedeliveryLedgerValidator {
    return SeasonalRedeliveryLedgerValidator(
        mutableListOf<SeasonalRedeliveryLedgerRule>(
            SeasonalRedeliveryLedgerAllowedCodeRule(mutableListOf<String>()),
            SeasonalRedeliveryLedgerRevisionRangeRule(16, 80),
            SeasonalRedeliveryLedgerCodePrefixRule("REF"),
            SeasonalRedeliveryLedgerCodeLengthRule(64),
        ),
    )
}
