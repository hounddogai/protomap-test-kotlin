package com.acme.transport.emissions.fuelburn.rule

/** A rule a tiered fuel burn ledger must pass. Check returns its problems, or none. */
interface TieredFuelBurnLedgerRule {
    fun check(item: TieredFuelBurnLedger): MutableList<String>
}

data class TieredFuelBurnLedgerPhoneLengthRule(var maxLength: Int) : TieredFuelBurnLedgerRule {
    override fun check(item: TieredFuelBurnLedger): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnLedgerCodePrefixRule(var prefix: String) : TieredFuelBurnLedgerRule {
    override fun check(item: TieredFuelBurnLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnLedgerOdometerRangeRule(var min: Int, var max: Int) : TieredFuelBurnLedgerRule {
    override fun check(item: TieredFuelBurnLedger): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnLedgerRequiredLabelRule(var message: String) : TieredFuelBurnLedgerRule {
    override fun check(item: TieredFuelBurnLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnLedgerChannelLengthRule(var maxLength: Int) : TieredFuelBurnLedgerRule {
    override fun check(item: TieredFuelBurnLedger): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel burn ledger. */
data class TieredFuelBurnLedgerValidator(var rules: MutableList<TieredFuelBurnLedgerRule>) {
    fun validate(item: TieredFuelBurnLedger): MutableList<String> {
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

    fun requireValid(item: TieredFuelBurnLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelBurnLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelBurnLedgerValidator(): TieredFuelBurnLedgerValidator {
    return TieredFuelBurnLedgerValidator(
        mutableListOf<TieredFuelBurnLedgerRule>(
            TieredFuelBurnLedgerPhoneLengthRule(255),
            TieredFuelBurnLedgerCodePrefixRule("ACM"),
            TieredFuelBurnLedgerOdometerRangeRule(1000, 3600),
            TieredFuelBurnLedgerRequiredLabelRule("tiered fuel burn ledger needs this field"),
            TieredFuelBurnLedgerChannelLengthRule(3600),
        ),
    )
}
