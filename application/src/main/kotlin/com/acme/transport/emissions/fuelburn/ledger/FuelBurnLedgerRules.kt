package com.acme.transport.emissions.fuelburn.ledger

/** A rule a fuel burn ledger must pass. Check returns its problems, or none. */
interface FuelBurnLedgerRule {
    fun check(item: FuelBurnLedger): MutableList<String>
}

data class FuelBurnLedgerRequiredNotesRule(var message: String) : FuelBurnLedgerRule {
    override fun check(item: FuelBurnLedger): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnLedgerRequiredNameRule(var message: String) : FuelBurnLedgerRule {
    override fun check(item: FuelBurnLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnLedgerCodeLengthRule(var maxLength: Int) : FuelBurnLedgerRule {
    override fun check(item: FuelBurnLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnLedgerCodePrefixRule(var prefix: String) : FuelBurnLedgerRule {
    override fun check(item: FuelBurnLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnLedgerUnitLengthRule(var maxLength: Int) : FuelBurnLedgerRule {
    override fun check(item: FuelBurnLedger): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn ledger. */
data class FuelBurnLedgerValidator(var rules: MutableList<FuelBurnLedgerRule>) {
    fun validate(item: FuelBurnLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnLedgerValidator(): FuelBurnLedgerValidator {
    return FuelBurnLedgerValidator(
        mutableListOf<FuelBurnLedgerRule>(
            FuelBurnLedgerRequiredNotesRule("fuel burn ledger needs this field"),
            FuelBurnLedgerRequiredNameRule("fuel burn ledger needs a value"),
            FuelBurnLedgerCodeLengthRule(86400),
            FuelBurnLedgerCodePrefixRule("REF"),
            FuelBurnLedgerUnitLengthRule(255),
        ),
    )
}
