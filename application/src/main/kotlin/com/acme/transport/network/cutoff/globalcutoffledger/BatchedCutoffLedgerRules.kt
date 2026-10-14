package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.text.normalize

/** A rule a batched cutoff ledger must pass. Check returns its problems, or none. */
interface BatchedCutoffLedgerRule {
    fun check(item: BatchedCutoffLedger): MutableList<String>
}

data class BatchedCutoffLedgerMarginRangeRule(var min: Double, var max: Double) : BatchedCutoffLedgerRule {
    override fun check(item: BatchedCutoffLedger): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class BatchedCutoffLedgerTitleLengthRule(var maxLength: Int) : BatchedCutoffLedgerRule {
    override fun check(item: BatchedCutoffLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCutoffLedgerRequiredTitleRule(var message: String) : BatchedCutoffLedgerRule {
    override fun check(item: BatchedCutoffLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCutoffLedgerCodeLengthRule(var maxLength: Int) : BatchedCutoffLedgerRule {
    override fun check(item: BatchedCutoffLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCutoffLedgerAllowedTitleRule(var allowed: MutableList<String>) : BatchedCutoffLedgerRule {
    override fun check(item: BatchedCutoffLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched cutoff ledger. */
data class BatchedCutoffLedgerValidator(var rules: MutableList<BatchedCutoffLedgerRule>) {
    fun validate(item: BatchedCutoffLedger): MutableList<String> {
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

    fun requireValid(item: BatchedCutoffLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCutoffLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCutoffLedgerValidator(): BatchedCutoffLedgerValidator {
    return BatchedCutoffLedgerValidator(
        mutableListOf<BatchedCutoffLedgerRule>(
            BatchedCutoffLedgerMarginRangeRule(3.4, 4049.6),
            BatchedCutoffLedgerTitleLengthRule(255),
            BatchedCutoffLedgerRequiredTitleRule("batched cutoff ledger needs a value"),
            BatchedCutoffLedgerCodeLengthRule(80),
            BatchedCutoffLedgerAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
