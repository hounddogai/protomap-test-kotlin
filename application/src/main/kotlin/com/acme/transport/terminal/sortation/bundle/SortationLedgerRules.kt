package com.acme.transport.terminal.sortation.bundle

/** A rule a sortation ledger must pass. Check returns its problems, or none. */
interface SortationLedgerRule {
    fun check(item: SortationLedger): MutableList<String>
}

data class SortationLedgerCodePrefixRule(var prefix: String) : SortationLedgerRule {
    override fun check(item: SortationLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationLedgerRequiredReviewerRule(var message: String) : SortationLedgerRule {
    override fun check(item: SortationLedger): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationLedgerCodeLengthRule(var maxLength: Int) : SortationLedgerRule {
    override fun check(item: SortationLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationLedgerRequiredTitleRule(var message: String) : SortationLedgerRule {
    override fun check(item: SortationLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation ledger. */
data class SortationLedgerValidator(var rules: MutableList<SortationLedgerRule>) {
    fun validate(item: SortationLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationLedgerValidator(): SortationLedgerValidator {
    return SortationLedgerValidator(
        mutableListOf<SortationLedgerRule>(
            SortationLedgerCodePrefixRule("REF"),
            SortationLedgerRequiredReviewerRule("sortation ledger needs a value"),
            SortationLedgerCodeLengthRule(120),
            SortationLedgerRequiredTitleRule("sortation ledger needs a value"),
        ),
    )
}
