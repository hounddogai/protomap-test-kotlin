package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.text.normalize

/** A rule a primary utilization ledger must pass. Check returns its problems, or none. */
interface PrimaryUtilizationLedgerRule {
    fun check(item: PrimaryUtilizationLedger): MutableList<String>
}

data class PrimaryUtilizationLedgerAllowedLabelRule(
    var allowed: MutableList<String>,
) : PrimaryUtilizationLedgerRule {
    override fun check(item: PrimaryUtilizationLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationLedgerRequiredChannelRule(var message: String) : PrimaryUtilizationLedgerRule {
    override fun check(item: PrimaryUtilizationLedger): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationLedgerChannelLengthRule(var maxLength: Int) : PrimaryUtilizationLedgerRule {
    override fun check(item: PrimaryUtilizationLedger): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationLedgerCostRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryUtilizationLedgerRule {
    override fun check(item: PrimaryUtilizationLedger): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary utilization ledger. */
data class PrimaryUtilizationLedgerValidator(var rules: MutableList<PrimaryUtilizationLedgerRule>) {
    fun validate(item: PrimaryUtilizationLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryUtilizationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryUtilizationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryUtilizationLedgerValidator(): PrimaryUtilizationLedgerValidator {
    return PrimaryUtilizationLedgerValidator(
        mutableListOf<PrimaryUtilizationLedgerRule>(
            PrimaryUtilizationLedgerAllowedLabelRule(mutableListOf<String>("legacy")),
            PrimaryUtilizationLedgerRequiredChannelRule("primary utilization ledger needs a value"),
            PrimaryUtilizationLedgerChannelLengthRule(32),
            PrimaryUtilizationLedgerCostRangeRule(5.5, 3193.6),
        ),
    )
}
