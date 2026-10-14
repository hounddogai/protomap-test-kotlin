package com.acme.transport.network.node.ledger

/** A rule a tiered node ledger must pass. Check returns its problems, or none. */
interface TieredNodeLedgerRule {
    fun check(item: TieredNodeLedger): MutableList<String>
}

data class TieredNodeLedgerLabelLengthRule(var maxLength: Int) : TieredNodeLedgerRule {
    override fun check(item: TieredNodeLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeLedgerCodePrefixRule(var prefix: String) : TieredNodeLedgerRule {
    override fun check(item: TieredNodeLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeLedgerRequiredDepotRule(var message: String) : TieredNodeLedgerRule {
    override fun check(item: TieredNodeLedger): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeLedgerRateRangeRule(var min: Double, var max: Double) : TieredNodeLedgerRule {
    override fun check(item: TieredNodeLedger): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class TieredNodeLedgerCodeLengthRule(var maxLength: Int) : TieredNodeLedgerRule {
    override fun check(item: TieredNodeLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered node ledger. */
data class TieredNodeLedgerValidator(var rules: MutableList<TieredNodeLedgerRule>) {
    fun validate(item: TieredNodeLedger): MutableList<String> {
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

    fun requireValid(item: TieredNodeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredNodeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredNodeLedgerValidator(): TieredNodeLedgerValidator {
    return TieredNodeLedgerValidator(
        mutableListOf<TieredNodeLedgerRule>(
            TieredNodeLedgerLabelLengthRule(255),
            TieredNodeLedgerCodePrefixRule("OPS"),
            TieredNodeLedgerRequiredDepotRule("tiered node ledger needs this field"),
            TieredNodeLedgerRateRangeRule(9.3, 3896.5),
            TieredNodeLedgerCodeLengthRule(120),
        ),
    )
}
