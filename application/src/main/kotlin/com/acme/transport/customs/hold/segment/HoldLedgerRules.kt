package com.acme.transport.customs.hold.segment

/** A rule a hold ledger must pass. Check returns its problems, or none. */
interface HoldLedgerRule {
    fun check(item: HoldLedger): MutableList<String>
}

data class HoldLedgerLoadKgRangeRule(var min: Double, var max: Double) : HoldLedgerRule {
    override fun check(item: HoldLedger): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldLedgerRequiredLabelRule(var message: String) : HoldLedgerRule {
    override fun check(item: HoldLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldLedgerSummaryLengthRule(var maxLength: Int) : HoldLedgerRule {
    override fun check(item: HoldLedger): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold ledger. */
data class HoldLedgerValidator(var rules: MutableList<HoldLedgerRule>) {
    fun validate(item: HoldLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldLedgerValidator(): HoldLedgerValidator {
    return HoldLedgerValidator(
        mutableListOf<HoldLedgerRule>(
            HoldLedgerLoadKgRangeRule(3.9, 1669.3),
            HoldLedgerRequiredLabelRule("hold ledger needs an entry"),
            HoldLedgerSummaryLengthRule(3600),
        ),
    )
}
