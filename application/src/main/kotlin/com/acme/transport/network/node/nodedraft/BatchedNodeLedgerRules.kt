package com.acme.transport.network.node.nodedraft

/** A rule a batched node ledger must pass. Check returns its problems, or none. */
interface BatchedNodeLedgerRule {
    fun check(item: BatchedNodeLedger): MutableList<String>
}

data class BatchedNodeLedgerPhoneLengthRule(var maxLength: Int) : BatchedNodeLedgerRule {
    override fun check(item: BatchedNodeLedger): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeLedgerRequiredReviewerRule(var message: String) : BatchedNodeLedgerRule {
    override fun check(item: BatchedNodeLedger): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeLedgerReviewerLengthRule(var maxLength: Int) : BatchedNodeLedgerRule {
    override fun check(item: BatchedNodeLedger): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeLedgerCodePrefixRule(var prefix: String) : BatchedNodeLedgerRule {
    override fun check(item: BatchedNodeLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched node ledger. */
data class BatchedNodeLedgerValidator(var rules: MutableList<BatchedNodeLedgerRule>) {
    fun validate(item: BatchedNodeLedger): MutableList<String> {
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

    fun requireValid(item: BatchedNodeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNodeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNodeLedgerValidator(): BatchedNodeLedgerValidator {
    return BatchedNodeLedgerValidator(
        mutableListOf<BatchedNodeLedgerRule>(
            BatchedNodeLedgerPhoneLengthRule(64),
            BatchedNodeLedgerRequiredReviewerRule("batched node ledger needs an entry"),
            BatchedNodeLedgerReviewerLengthRule(16),
            BatchedNodeLedgerCodePrefixRule("Q"),
        ),
    )
}
