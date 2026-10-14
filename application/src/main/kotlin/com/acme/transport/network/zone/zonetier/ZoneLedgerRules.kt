package com.acme.transport.network.zone.zonetier

/** A rule a zone ledger must pass. Check returns its problems, or none. */
interface ZoneLedgerRule {
    fun check(item: ZoneLedger): MutableList<String>
}

data class ZoneLedgerRequiredCodeRule(var message: String) : ZoneLedgerRule {
    override fun check(item: ZoneLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneLedgerFuelLitersRangeRule(var min: Double, var max: Double) : ZoneLedgerRule {
    override fun check(item: ZoneLedger): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ZoneLedgerPalletsRangeRule(var min: Int, var max: Int) : ZoneLedgerRule {
    override fun check(item: ZoneLedger): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneLedgerRequiredTitleRule(var message: String) : ZoneLedgerRule {
    override fun check(item: ZoneLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone ledger. */
data class ZoneLedgerValidator(var rules: MutableList<ZoneLedgerRule>) {
    fun validate(item: ZoneLedger): MutableList<String> {
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

    fun requireValid(item: ZoneLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneLedgerValidator(): ZoneLedgerValidator {
    return ZoneLedgerValidator(
        mutableListOf<ZoneLedgerRule>(
            ZoneLedgerRequiredCodeRule("zone ledger needs an entry"),
            ZoneLedgerFuelLitersRangeRule(5.0, 3787.3),
            ZoneLedgerPalletsRangeRule(80, 120),
            ZoneLedgerRequiredTitleRule("zone ledger needs this field"),
        ),
    )
}
