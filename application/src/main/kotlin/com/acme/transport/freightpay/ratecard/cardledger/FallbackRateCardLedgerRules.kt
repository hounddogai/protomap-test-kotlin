package com.acme.transport.freightpay.ratecard.cardledger

/** A rule a fallback rate card ledger must pass. Check returns its problems, or none. */
interface FallbackRateCardLedgerRule {
    fun check(item: FallbackRateCardLedger): MutableList<String>
}

data class FallbackRateCardLedgerAttemptsRangeRule(var min: Int, var max: Int) : FallbackRateCardLedgerRule {
    override fun check(item: FallbackRateCardLedger): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardLedgerRequiredTitleRule(var message: String) : FallbackRateCardLedgerRule {
    override fun check(item: FallbackRateCardLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardLedgerRequiredCodeRule(var message: String) : FallbackRateCardLedgerRule {
    override fun check(item: FallbackRateCardLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardLedgerTitleLengthRule(var maxLength: Int) : FallbackRateCardLedgerRule {
    override fun check(item: FallbackRateCardLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardLedgerCodeLengthRule(var maxLength: Int) : FallbackRateCardLedgerRule {
    override fun check(item: FallbackRateCardLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback rate card ledger. */
data class FallbackRateCardLedgerValidator(var rules: MutableList<FallbackRateCardLedgerRule>) {
    fun validate(item: FallbackRateCardLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRateCardLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRateCardLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRateCardLedgerValidator(): FallbackRateCardLedgerValidator {
    return FallbackRateCardLedgerValidator(
        mutableListOf<FallbackRateCardLedgerRule>(
            FallbackRateCardLedgerAttemptsRangeRule(16, 64),
            FallbackRateCardLedgerRequiredTitleRule("fallback rate card ledger needs this field"),
            FallbackRateCardLedgerRequiredCodeRule("fallback rate card ledger needs an entry"),
            FallbackRateCardLedgerTitleLengthRule(64),
            FallbackRateCardLedgerCodeLengthRule(16),
        ),
    )
}
