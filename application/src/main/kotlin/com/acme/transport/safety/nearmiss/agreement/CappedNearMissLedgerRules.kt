package com.acme.transport.safety.nearmiss.agreement

/** A rule a capped near miss ledger must pass. Check returns its problems, or none. */
interface CappedNearMissLedgerRule {
    fun check(item: CappedNearMissLedger): MutableList<String>
}

data class CappedNearMissLedgerLaneLengthRule(var maxLength: Int) : CappedNearMissLedgerRule {
    override fun check(item: CappedNearMissLedger): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissLedgerChannelLengthRule(var maxLength: Int) : CappedNearMissLedgerRule {
    override fun check(item: CappedNearMissLedger): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissLedgerCodeLengthRule(var maxLength: Int) : CappedNearMissLedgerRule {
    override fun check(item: CappedNearMissLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped near miss ledger. */
data class CappedNearMissLedgerValidator(var rules: MutableList<CappedNearMissLedgerRule>) {
    fun validate(item: CappedNearMissLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedNearMissLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedNearMissLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedNearMissLedgerValidator(): CappedNearMissLedgerValidator {
    return CappedNearMissLedgerValidator(
        mutableListOf<CappedNearMissLedgerRule>(
            CappedNearMissLedgerLaneLengthRule(255),
            CappedNearMissLedgerChannelLengthRule(255),
            CappedNearMissLedgerCodeLengthRule(1000),
        ),
    )
}
