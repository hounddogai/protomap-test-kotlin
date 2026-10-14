package com.acme.transport.crewing.swap.rule

/** A rule a swap ledger must pass. Check returns its problems, or none. */
interface SwapLedgerRule {
    fun check(item: SwapLedger): MutableList<String>
}

data class SwapLedgerCodeLengthRule(var maxLength: Int) : SwapLedgerRule {
    override fun check(item: SwapLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapLedgerExternalRefLengthRule(var maxLength: Int) : SwapLedgerRule {
    override fun check(item: SwapLedger): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapLedgerRequiredCodeRule(var message: String) : SwapLedgerRule {
    override fun check(item: SwapLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapLedgerAmountRangeRule(var min: Double, var max: Double) : SwapLedgerRule {
    override fun check(item: SwapLedger): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapLedgerRequiredExternalRefRule(var message: String) : SwapLedgerRule {
    override fun check(item: SwapLedger): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapLedgerTitleLengthRule(var maxLength: Int) : SwapLedgerRule {
    override fun check(item: SwapLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap ledger. */
data class SwapLedgerValidator(var rules: MutableList<SwapLedgerRule>) {
    fun validate(item: SwapLedger): MutableList<String> {
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

    fun requireValid(item: SwapLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapLedgerValidator(): SwapLedgerValidator {
    return SwapLedgerValidator(
        mutableListOf<SwapLedgerRule>(
            SwapLedgerCodeLengthRule(3600),
            SwapLedgerExternalRefLengthRule(86400),
            SwapLedgerRequiredCodeRule("swap ledger needs an entry"),
            SwapLedgerAmountRangeRule(5.3, 778.8),
            SwapLedgerRequiredExternalRefRule("swap ledger needs an entry"),
            SwapLedgerTitleLengthRule(255),
        ),
    )
}
