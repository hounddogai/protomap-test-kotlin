package com.acme.transport.fleetops.leasereturn.returnledger

/** A rule a seasonal lease return ledger must pass. Check returns its problems, or none. */
interface SeasonalLeaseReturnLedgerRule {
    fun check(item: SeasonalLeaseReturnLedger): MutableList<String>
}

data class SeasonalLeaseReturnLedgerRequiredCodeRule(var message: String) : SeasonalLeaseReturnLedgerRule {
    override fun check(item: SeasonalLeaseReturnLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLeaseReturnLedgerCodeLengthRule(var maxLength: Int) : SeasonalLeaseReturnLedgerRule {
    override fun check(item: SeasonalLeaseReturnLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLeaseReturnLedgerLimitRangeRule(var min: Int, var max: Int) : SeasonalLeaseReturnLedgerRule {
    override fun check(item: SeasonalLeaseReturnLedger): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class SeasonalLeaseReturnLedgerDockLengthRule(var maxLength: Int) : SeasonalLeaseReturnLedgerRule {
    override fun check(item: SeasonalLeaseReturnLedger): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLeaseReturnLedgerRegionLengthRule(var maxLength: Int) : SeasonalLeaseReturnLedgerRule {
    override fun check(item: SeasonalLeaseReturnLedger): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLeaseReturnLedgerScoreRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalLeaseReturnLedgerRule {
    override fun check(item: SeasonalLeaseReturnLedger): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal lease return ledger. */
data class SeasonalLeaseReturnLedgerValidator(var rules: MutableList<SeasonalLeaseReturnLedgerRule>) {
    fun validate(item: SeasonalLeaseReturnLedger): MutableList<String> {
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

    fun requireValid(item: SeasonalLeaseReturnLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalLeaseReturnLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalLeaseReturnLedgerValidator(): SeasonalLeaseReturnLedgerValidator {
    return SeasonalLeaseReturnLedgerValidator(
        mutableListOf<SeasonalLeaseReturnLedgerRule>(
            SeasonalLeaseReturnLedgerRequiredCodeRule("seasonal lease return ledger needs this field"),
            SeasonalLeaseReturnLedgerCodeLengthRule(80),
            SeasonalLeaseReturnLedgerLimitRangeRule(16, 3600),
            SeasonalLeaseReturnLedgerDockLengthRule(1000),
            SeasonalLeaseReturnLedgerRegionLengthRule(120),
            SeasonalLeaseReturnLedgerScoreRangeRule(4.5, 555.7),
        ),
    )
}
