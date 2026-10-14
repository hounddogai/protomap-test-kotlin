package com.acme.transport.freightpay.dispute.estimate

/** A rule a dispute ledger must pass. Check returns its problems, or none. */
interface DisputeLedgerRule {
    fun check(item: DisputeLedger): MutableList<String>
}

data class DisputeLedgerRequiredPhoneRule(var message: String) : DisputeLedgerRule {
    override fun check(item: DisputeLedger): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeLedgerTitleLengthRule(var maxLength: Int) : DisputeLedgerRule {
    override fun check(item: DisputeLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeLedgerLaneLengthRule(var maxLength: Int) : DisputeLedgerRule {
    override fun check(item: DisputeLedger): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute ledger. */
data class DisputeLedgerValidator(var rules: MutableList<DisputeLedgerRule>) {
    fun validate(item: DisputeLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeLedgerValidator(): DisputeLedgerValidator {
    return DisputeLedgerValidator(
        mutableListOf<DisputeLedgerRule>(
            DisputeLedgerRequiredPhoneRule("dispute ledger needs this field"),
            DisputeLedgerTitleLengthRule(120),
            DisputeLedgerLaneLengthRule(32),
        ),
    )
}
