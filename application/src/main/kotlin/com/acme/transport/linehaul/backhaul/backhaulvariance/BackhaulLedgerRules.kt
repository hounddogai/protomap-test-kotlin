package com.acme.transport.linehaul.backhaul.backhaulvariance

/** A rule a backhaul ledger must pass. Check returns its problems, or none. */
interface BackhaulLedgerRule {
    fun check(item: BackhaulLedger): MutableList<String>
}

data class BackhaulLedgerSequenceRangeRule(var min: Int, var max: Int) : BackhaulLedgerRule {
    override fun check(item: BackhaulLedger): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class BackhaulLedgerCodePrefixRule(var prefix: String) : BackhaulLedgerRule {
    override fun check(item: BackhaulLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulLedgerCodeLengthRule(var maxLength: Int) : BackhaulLedgerRule {
    override fun check(item: BackhaulLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul ledger. */
data class BackhaulLedgerValidator(var rules: MutableList<BackhaulLedgerRule>) {
    fun validate(item: BackhaulLedger): MutableList<String> {
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

    fun requireValid(item: BackhaulLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulLedgerValidator(): BackhaulLedgerValidator {
    return BackhaulLedgerValidator(
        mutableListOf<BackhaulLedgerRule>(
            BackhaulLedgerSequenceRangeRule(120, 255),
            BackhaulLedgerCodePrefixRule("REF"),
            BackhaulLedgerCodeLengthRule(32),
        ),
    )
}
