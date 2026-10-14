package com.acme.transport.fleetops.leasereturn.estimate

import com.acme.transport.kit.text.normalize

/** A rule a legacy lease return ledger must pass. Check returns its problems, or none. */
interface LegacyLeaseReturnLedgerRule {
    fun check(item: LegacyLeaseReturnLedger): MutableList<String>
}

data class LegacyLeaseReturnLedgerAllowedDepotRule(
    var allowed: MutableList<String>,
) : LegacyLeaseReturnLedgerRule {
    override fun check(item: LegacyLeaseReturnLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnLedgerPinnedGuardRule(var limit: Int) : LegacyLeaseReturnLedgerRule {
    override fun check(item: LegacyLeaseReturnLedger): MutableList<String> {
        if (item.pinned && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnLedgerStartsAtRangeRule(var min: Int, var max: Int) : LegacyLeaseReturnLedgerRule {
    override fun check(item: LegacyLeaseReturnLedger): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnLedgerRequiredDepotRule(var message: String) : LegacyLeaseReturnLedgerRule {
    override fun check(item: LegacyLeaseReturnLedger): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy lease return ledger. */
data class LegacyLeaseReturnLedgerValidator(var rules: MutableList<LegacyLeaseReturnLedgerRule>) {
    fun validate(item: LegacyLeaseReturnLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyLeaseReturnLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLeaseReturnLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLeaseReturnLedgerValidator(): LegacyLeaseReturnLedgerValidator {
    return LegacyLeaseReturnLedgerValidator(
        mutableListOf<LegacyLeaseReturnLedgerRule>(
            LegacyLeaseReturnLedgerAllowedDepotRule(mutableListOf<String>()),
            LegacyLeaseReturnLedgerPinnedGuardRule(80),
            LegacyLeaseReturnLedgerStartsAtRangeRule(16, 16),
            LegacyLeaseReturnLedgerRequiredDepotRule("legacy lease return ledger needs this field"),
        ),
    )
}
