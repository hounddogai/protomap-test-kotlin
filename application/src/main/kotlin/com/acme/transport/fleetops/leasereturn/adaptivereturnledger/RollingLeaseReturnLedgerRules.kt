package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.text.normalize

/** A rule a rolling lease return ledger must pass. Check returns its problems, or none. */
interface RollingLeaseReturnLedgerRule {
    fun check(item: RollingLeaseReturnLedger): MutableList<String>
}

data class RollingLeaseReturnLedgerRequiredCodeRule(var message: String) : RollingLeaseReturnLedgerRule {
    override fun check(item: RollingLeaseReturnLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingLeaseReturnLedgerRateRangeRule(
    var min: Double,
    var max: Double,
) : RollingLeaseReturnLedgerRule {
    override fun check(item: RollingLeaseReturnLedger): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingLeaseReturnLedgerCodePrefixRule(var prefix: String) : RollingLeaseReturnLedgerRule {
    override fun check(item: RollingLeaseReturnLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingLeaseReturnLedgerMinimumRangeRule(
    var min: Double,
    var max: Double,
) : RollingLeaseReturnLedgerRule {
    override fun check(item: RollingLeaseReturnLedger): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class RollingLeaseReturnLedgerWeightRangeRule(
    var min: Double,
    var max: Double,
) : RollingLeaseReturnLedgerRule {
    override fun check(item: RollingLeaseReturnLedger): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class RollingLeaseReturnLedgerAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingLeaseReturnLedgerRule {
    override fun check(item: RollingLeaseReturnLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling lease return ledger. */
data class RollingLeaseReturnLedgerValidator(var rules: MutableList<RollingLeaseReturnLedgerRule>) {
    fun validate(item: RollingLeaseReturnLedger): MutableList<String> {
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

    fun requireValid(item: RollingLeaseReturnLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingLeaseReturnLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingLeaseReturnLedgerValidator(): RollingLeaseReturnLedgerValidator {
    return RollingLeaseReturnLedgerValidator(
        mutableListOf<RollingLeaseReturnLedgerRule>(
            RollingLeaseReturnLedgerRequiredCodeRule("rolling lease return ledger needs this field"),
            RollingLeaseReturnLedgerRateRangeRule(0.9, 2571.6),
            RollingLeaseReturnLedgerCodePrefixRule("X"),
            RollingLeaseReturnLedgerMinimumRangeRule(4.2, 562.4),
            RollingLeaseReturnLedgerWeightRangeRule(7.2, 2514.2),
            RollingLeaseReturnLedgerAllowedCodeRule(mutableListOf<String>("fallback")),
        ),
    )
}
