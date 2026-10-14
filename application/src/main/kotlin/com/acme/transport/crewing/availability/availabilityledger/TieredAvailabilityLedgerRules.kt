package com.acme.transport.crewing.availability.availabilityledger

/** A rule a tiered availability ledger must pass. Check returns its problems, or none. */
interface TieredAvailabilityLedgerRule {
    fun check(item: TieredAvailabilityLedger): MutableList<String>
}

data class TieredAvailabilityLedgerPriorityRangeRule(
    var min: Int,
    var max: Int,
) : TieredAvailabilityLedgerRule {
    override fun check(item: TieredAvailabilityLedger): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class TieredAvailabilityLedgerDockLengthRule(var maxLength: Int) : TieredAvailabilityLedgerRule {
    override fun check(item: TieredAvailabilityLedger): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAvailabilityLedgerRequiredTitleRule(var message: String) : TieredAvailabilityLedgerRule {
    override fun check(item: TieredAvailabilityLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAvailabilityLedgerCodeLengthRule(var maxLength: Int) : TieredAvailabilityLedgerRule {
    override fun check(item: TieredAvailabilityLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAvailabilityLedgerExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : TieredAvailabilityLedgerRule {
    override fun check(item: TieredAvailabilityLedger): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class TieredAvailabilityLedgerRequiredDockRule(var message: String) : TieredAvailabilityLedgerRule {
    override fun check(item: TieredAvailabilityLedger): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered availability ledger. */
data class TieredAvailabilityLedgerValidator(var rules: MutableList<TieredAvailabilityLedgerRule>) {
    fun validate(item: TieredAvailabilityLedger): MutableList<String> {
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

    fun requireValid(item: TieredAvailabilityLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAvailabilityLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAvailabilityLedgerValidator(): TieredAvailabilityLedgerValidator {
    return TieredAvailabilityLedgerValidator(
        mutableListOf<TieredAvailabilityLedgerRule>(
            TieredAvailabilityLedgerPriorityRangeRule(32, 64),
            TieredAvailabilityLedgerDockLengthRule(16),
            TieredAvailabilityLedgerRequiredTitleRule("tiered availability ledger needs this field"),
            TieredAvailabilityLedgerCodeLengthRule(86400),
            TieredAvailabilityLedgerExpiresAtRangeRule(32, 3600),
            TieredAvailabilityLedgerRequiredDockRule("tiered availability ledger needs a value"),
        ),
    )
}
