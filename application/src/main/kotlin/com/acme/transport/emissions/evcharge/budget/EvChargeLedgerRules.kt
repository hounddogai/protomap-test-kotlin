package com.acme.transport.emissions.evcharge.budget

/** A rule a ev charge ledger must pass. Check returns its problems, or none. */
interface EvChargeLedgerRule {
    fun check(item: EvChargeLedger): MutableList<String>
}

data class EvChargeLedgerExternalRefLengthRule(var maxLength: Int) : EvChargeLedgerRule {
    override fun check(item: EvChargeLedger): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeLedgerThresholdRangeRule(var min: Double, var max: Double) : EvChargeLedgerRule {
    override fun check(item: EvChargeLedger): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeLedgerDepotLengthRule(var maxLength: Int) : EvChargeLedgerRule {
    override fun check(item: EvChargeLedger): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge ledger. */
data class EvChargeLedgerValidator(var rules: MutableList<EvChargeLedgerRule>) {
    fun validate(item: EvChargeLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeLedgerValidator(): EvChargeLedgerValidator {
    return EvChargeLedgerValidator(
        mutableListOf<EvChargeLedgerRule>(
            EvChargeLedgerExternalRefLengthRule(3600),
            EvChargeLedgerThresholdRangeRule(3.6, 4278.4),
            EvChargeLedgerDepotLengthRule(32),
        ),
    )
}
