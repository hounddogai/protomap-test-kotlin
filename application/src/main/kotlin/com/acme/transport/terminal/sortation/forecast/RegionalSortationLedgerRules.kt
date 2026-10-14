package com.acme.transport.terminal.sortation.forecast

/** A rule a regional sortation ledger must pass. Check returns its problems, or none. */
interface RegionalSortationLedgerRule {
    fun check(item: RegionalSortationLedger): MutableList<String>
}

data class RegionalSortationLedgerCodePrefixRule(var prefix: String) : RegionalSortationLedgerRule {
    override fun check(item: RegionalSortationLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationLedgerRequiredCodeRule(var message: String) : RegionalSortationLedgerRule {
    override fun check(item: RegionalSortationLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationLedgerLimitRangeRule(var min: Int, var max: Int) : RegionalSortationLedgerRule {
    override fun check(item: RegionalSortationLedger): MutableList<String> {
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

data class RegionalSortationLedgerCodeLengthRule(var maxLength: Int) : RegionalSortationLedgerRule {
    override fun check(item: RegionalSortationLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationLedgerFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RegionalSortationLedgerRule {
    override fun check(item: RegionalSortationLedger): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationLedgerCeilingRangeRule(
    var min: Double,
    var max: Double,
) : RegionalSortationLedgerRule {
    override fun check(item: RegionalSortationLedger): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional sortation ledger. */
data class RegionalSortationLedgerValidator(var rules: MutableList<RegionalSortationLedgerRule>) {
    fun validate(item: RegionalSortationLedger): MutableList<String> {
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

    fun requireValid(item: RegionalSortationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalSortationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalSortationLedgerValidator(): RegionalSortationLedgerValidator {
    return RegionalSortationLedgerValidator(
        mutableListOf<RegionalSortationLedgerRule>(
            RegionalSortationLedgerCodePrefixRule("ACM"),
            RegionalSortationLedgerRequiredCodeRule("regional sortation ledger needs a value"),
            RegionalSortationLedgerLimitRangeRule(64, 86400),
            RegionalSortationLedgerCodeLengthRule(64),
            RegionalSortationLedgerFuelLitersRangeRule(8.2, 2871.6),
            RegionalSortationLedgerCeilingRangeRule(0.4, 2454.9),
        ),
    )
}
