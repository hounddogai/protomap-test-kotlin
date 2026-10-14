package com.acme.transport.freightpay.ratecard.ledger

import com.acme.transport.kit.text.normalize

/** A rule a rate card ledger must pass. Check returns its problems, or none. */
interface RateCardLedgerRule {
    fun check(item: RateCardLedger): MutableList<String>
}

data class RateCardLedgerRequiredTitleRule(var message: String) : RateCardLedgerRule {
    override fun check(item: RateCardLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardLedgerAllowedDepotRule(var allowed: MutableList<String>) : RateCardLedgerRule {
    override fun check(item: RateCardLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RateCardLedgerLimitRangeRule(var min: Int, var max: Int) : RateCardLedgerRule {
    override fun check(item: RateCardLedger): MutableList<String> {
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

/** Runs every rule of a rate card ledger. */
data class RateCardLedgerValidator(var rules: MutableList<RateCardLedgerRule>) {
    fun validate(item: RateCardLedger): MutableList<String> {
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

    fun requireValid(item: RateCardLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardLedgerValidator(): RateCardLedgerValidator {
    return RateCardLedgerValidator(
        mutableListOf<RateCardLedgerRule>(
            RateCardLedgerRequiredTitleRule("rate card ledger needs an entry"),
            RateCardLedgerAllowedDepotRule(mutableListOf<String>("fallback", "capped", "scheduled")),
            RateCardLedgerLimitRangeRule(120, 1000),
        ),
    )
}
