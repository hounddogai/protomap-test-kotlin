package com.acme.transport.network.cutoff.cutoffledger

/** A rule a default cutoff ledger must pass. Check returns its problems, or none. */
interface DefaultCutoffLedgerRule {
    fun check(item: DefaultCutoffLedger): MutableList<String>
}

data class DefaultCutoffLedgerLabelLengthRule(var maxLength: Int) : DefaultCutoffLedgerRule {
    override fun check(item: DefaultCutoffLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffLedgerRequiredCodeRule(var message: String) : DefaultCutoffLedgerRule {
    override fun check(item: DefaultCutoffLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffLedgerPhoneLengthRule(var maxLength: Int) : DefaultCutoffLedgerRule {
    override fun check(item: DefaultCutoffLedger): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default cutoff ledger. */
data class DefaultCutoffLedgerValidator(var rules: MutableList<DefaultCutoffLedgerRule>) {
    fun validate(item: DefaultCutoffLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCutoffLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCutoffLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCutoffLedgerValidator(): DefaultCutoffLedgerValidator {
    return DefaultCutoffLedgerValidator(
        mutableListOf<DefaultCutoffLedgerRule>(
            DefaultCutoffLedgerLabelLengthRule(3600),
            DefaultCutoffLedgerRequiredCodeRule("default cutoff ledger needs this field"),
            DefaultCutoffLedgerPhoneLengthRule(32),
        ),
    )
}
