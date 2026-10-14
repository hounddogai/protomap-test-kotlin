package com.acme.transport.customs.hold.rollup

/** A rule a adaptive hold ledger must pass. Check returns its problems, or none. */
interface AdaptiveHoldLedgerRule {
    fun check(item: AdaptiveHoldLedger): MutableList<String>
}

data class AdaptiveHoldLedgerLabelLengthRule(var maxLength: Int) : AdaptiveHoldLedgerRule {
    override fun check(item: AdaptiveHoldLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldLedgerCodePrefixRule(var prefix: String) : AdaptiveHoldLedgerRule {
    override fun check(item: AdaptiveHoldLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldLedgerCodeLengthRule(var maxLength: Int) : AdaptiveHoldLedgerRule {
    override fun check(item: AdaptiveHoldLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldLedgerReviewerLengthRule(var maxLength: Int) : AdaptiveHoldLedgerRule {
    override fun check(item: AdaptiveHoldLedger): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldLedgerRequiredCodeRule(var message: String) : AdaptiveHoldLedgerRule {
    override fun check(item: AdaptiveHoldLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive hold ledger. */
data class AdaptiveHoldLedgerValidator(var rules: MutableList<AdaptiveHoldLedgerRule>) {
    fun validate(item: AdaptiveHoldLedger): MutableList<String> {
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

    fun requireValid(item: AdaptiveHoldLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveHoldLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveHoldLedgerValidator(): AdaptiveHoldLedgerValidator {
    return AdaptiveHoldLedgerValidator(
        mutableListOf<AdaptiveHoldLedgerRule>(
            AdaptiveHoldLedgerLabelLengthRule(1000),
            AdaptiveHoldLedgerCodePrefixRule("REF"),
            AdaptiveHoldLedgerCodeLengthRule(120),
            AdaptiveHoldLedgerReviewerLengthRule(16),
            AdaptiveHoldLedgerRequiredCodeRule("adaptive hold ledger needs this field"),
        ),
    )
}
