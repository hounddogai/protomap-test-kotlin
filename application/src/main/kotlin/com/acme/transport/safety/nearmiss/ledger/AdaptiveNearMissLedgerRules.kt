package com.acme.transport.safety.nearmiss.ledger

/** A rule a adaptive near miss ledger must pass. Check returns its problems, or none. */
interface AdaptiveNearMissLedgerRule {
    fun check(item: AdaptiveNearMissLedger): MutableList<String>
}

data class AdaptiveNearMissLedgerCodePrefixRule(var prefix: String) : AdaptiveNearMissLedgerRule {
    override fun check(item: AdaptiveNearMissLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissLedgerCodeLengthRule(var maxLength: Int) : AdaptiveNearMissLedgerRule {
    override fun check(item: AdaptiveNearMissLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissLedgerVisibleGuardRule(var limit: Int) : AdaptiveNearMissLedgerRule {
    override fun check(item: AdaptiveNearMissLedger): MutableList<String> {
        if (item.visible && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissLedgerExpiresAtRangeRule(var min: Int, var max: Int) : AdaptiveNearMissLedgerRule {
    override fun check(item: AdaptiveNearMissLedger): MutableList<String> {
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

data class AdaptiveNearMissLedgerRequiredCodeRule(var message: String) : AdaptiveNearMissLedgerRule {
    override fun check(item: AdaptiveNearMissLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive near miss ledger. */
data class AdaptiveNearMissLedgerValidator(var rules: MutableList<AdaptiveNearMissLedgerRule>) {
    fun validate(item: AdaptiveNearMissLedger): MutableList<String> {
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

    fun requireValid(item: AdaptiveNearMissLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNearMissLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNearMissLedgerValidator(): AdaptiveNearMissLedgerValidator {
    return AdaptiveNearMissLedgerValidator(
        mutableListOf<AdaptiveNearMissLedgerRule>(
            AdaptiveNearMissLedgerCodePrefixRule("Q"),
            AdaptiveNearMissLedgerCodeLengthRule(1000),
            AdaptiveNearMissLedgerVisibleGuardRule(3600),
            AdaptiveNearMissLedgerExpiresAtRangeRule(80, 3600),
            AdaptiveNearMissLedgerRequiredCodeRule("adaptive near miss ledger needs a value"),
        ),
    )
}
