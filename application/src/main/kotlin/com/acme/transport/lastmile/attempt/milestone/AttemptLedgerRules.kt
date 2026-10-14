package com.acme.transport.lastmile.attempt.milestone

/** A rule a attempt ledger must pass. Check returns its problems, or none. */
interface AttemptLedgerRule {
    fun check(item: AttemptLedger): MutableList<String>
}

data class AttemptLedgerNameLengthRule(var maxLength: Int) : AttemptLedgerRule {
    override fun check(item: AttemptLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptLedgerRequiredCodeRule(var message: String) : AttemptLedgerRule {
    override fun check(item: AttemptLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptLedgerCodePrefixRule(var prefix: String) : AttemptLedgerRule {
    override fun check(item: AttemptLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt ledger. */
data class AttemptLedgerValidator(var rules: MutableList<AttemptLedgerRule>) {
    fun validate(item: AttemptLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptLedgerValidator(): AttemptLedgerValidator {
    return AttemptLedgerValidator(
        mutableListOf<AttemptLedgerRule>(
            AttemptLedgerNameLengthRule(64),
            AttemptLedgerRequiredCodeRule("attempt ledger needs an entry"),
            AttemptLedgerCodePrefixRule("REF"),
        ),
    )
}
