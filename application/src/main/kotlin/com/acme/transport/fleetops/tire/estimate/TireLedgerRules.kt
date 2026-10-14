package com.acme.transport.fleetops.tire.estimate

/** A rule a tire ledger must pass. Check returns its problems, or none. */
interface TireLedgerRule {
    fun check(item: TireLedger): MutableList<String>
}

data class TireLedgerRequiredCodeRule(var message: String) : TireLedgerRule {
    override fun check(item: TireLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireLedgerRequiredNameRule(var message: String) : TireLedgerRule {
    override fun check(item: TireLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireLedgerThresholdRangeRule(var min: Double, var max: Double) : TireLedgerRule {
    override fun check(item: TireLedger): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class TireLedgerRequiredUnitRule(var message: String) : TireLedgerRule {
    override fun check(item: TireLedger): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireLedgerNameLengthRule(var maxLength: Int) : TireLedgerRule {
    override fun check(item: TireLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire ledger. */
data class TireLedgerValidator(var rules: MutableList<TireLedgerRule>) {
    fun validate(item: TireLedger): MutableList<String> {
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

    fun requireValid(item: TireLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultTireLedgerValidator(): TireLedgerValidator {
    return TireLedgerValidator(
        mutableListOf<TireLedgerRule>(
            TireLedgerRequiredCodeRule("tire ledger needs a value"),
            TireLedgerRequiredNameRule("tire ledger needs a value"),
            TireLedgerThresholdRangeRule(2.0, 1050.3),
            TireLedgerRequiredUnitRule("tire ledger needs an entry"),
            TireLedgerNameLengthRule(1000),
        ),
    )
}
