package com.acme.transport.crewing.swap.slot

/** A rule a rolling swap ledger must pass. Check returns its problems, or none. */
interface RollingSwapLedgerRule {
    fun check(item: RollingSwapLedger): MutableList<String>
}

data class RollingSwapLedgerCodePrefixRule(var prefix: String) : RollingSwapLedgerRule {
    override fun check(item: RollingSwapLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapLedgerNotesLengthRule(var maxLength: Int) : RollingSwapLedgerRule {
    override fun check(item: RollingSwapLedger): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapLedgerRequiredLabelRule(var message: String) : RollingSwapLedgerRule {
    override fun check(item: RollingSwapLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapLedgerCodeLengthRule(var maxLength: Int) : RollingSwapLedgerRule {
    override fun check(item: RollingSwapLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling swap ledger. */
data class RollingSwapLedgerValidator(var rules: MutableList<RollingSwapLedgerRule>) {
    fun validate(item: RollingSwapLedger): MutableList<String> {
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

    fun requireValid(item: RollingSwapLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingSwapLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingSwapLedgerValidator(): RollingSwapLedgerValidator {
    return RollingSwapLedgerValidator(
        mutableListOf<RollingSwapLedgerRule>(
            RollingSwapLedgerCodePrefixRule("X"),
            RollingSwapLedgerNotesLengthRule(255),
            RollingSwapLedgerRequiredLabelRule("rolling swap ledger needs an entry"),
            RollingSwapLedgerCodeLengthRule(16),
        ),
    )
}
