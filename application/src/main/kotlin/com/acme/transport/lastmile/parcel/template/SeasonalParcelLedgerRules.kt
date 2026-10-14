package com.acme.transport.lastmile.parcel.template

/** A rule a seasonal parcel ledger must pass. Check returns its problems, or none. */
interface SeasonalParcelLedgerRule {
    fun check(item: SeasonalParcelLedger): MutableList<String>
}

data class SeasonalParcelLedgerPinnedGuardRule(var limit: Double) : SeasonalParcelLedgerRule {
    override fun check(item: SeasonalParcelLedger): MutableList<String> {
        if (item.pinned && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelLedgerCodePrefixRule(var prefix: String) : SeasonalParcelLedgerRule {
    override fun check(item: SeasonalParcelLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelLedgerCodeLengthRule(var maxLength: Int) : SeasonalParcelLedgerRule {
    override fun check(item: SeasonalParcelLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelLedgerRequiredNameRule(var message: String) : SeasonalParcelLedgerRule {
    override fun check(item: SeasonalParcelLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelLedgerAmountRangeRule(var min: Double, var max: Double) : SeasonalParcelLedgerRule {
    override fun check(item: SeasonalParcelLedger): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelLedgerRequiredCodeRule(var message: String) : SeasonalParcelLedgerRule {
    override fun check(item: SeasonalParcelLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal parcel ledger. */
data class SeasonalParcelLedgerValidator(var rules: MutableList<SeasonalParcelLedgerRule>) {
    fun validate(item: SeasonalParcelLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalParcelLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalParcelLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalParcelLedgerValidator(): SeasonalParcelLedgerValidator {
    return SeasonalParcelLedgerValidator(
        mutableListOf<SeasonalParcelLedgerRule>(
            SeasonalParcelLedgerPinnedGuardRule(6.4),
            SeasonalParcelLedgerCodePrefixRule("ACM"),
            SeasonalParcelLedgerCodeLengthRule(32),
            SeasonalParcelLedgerRequiredNameRule("seasonal parcel ledger needs an entry"),
            SeasonalParcelLedgerAmountRangeRule(7.0, 825.7),
            SeasonalParcelLedgerRequiredCodeRule("seasonal parcel ledger needs an entry"),
        ),
    )
}
