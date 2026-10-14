package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.normalize

/** A rule a adaptive backhaul ledger must pass. Check returns its problems, or none. */
interface AdaptiveBackhaulLedgerRule {
    fun check(item: AdaptiveBackhaulLedger): MutableList<String>
}

data class AdaptiveBackhaulLedgerRequiredCarrierRule(var message: String) : AdaptiveBackhaulLedgerRule {
    override fun check(item: AdaptiveBackhaulLedger): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulLedgerBudgetRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveBackhaulLedgerRule {
    override fun check(item: AdaptiveBackhaulLedger): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class AdaptiveBackhaulLedgerCarrierLengthRule(var maxLength: Int) : AdaptiveBackhaulLedgerRule {
    override fun check(item: AdaptiveBackhaulLedger): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulLedgerRequiredTitleRule(var message: String) : AdaptiveBackhaulLedgerRule {
    override fun check(item: AdaptiveBackhaulLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulLedgerAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveBackhaulLedgerRule {
    override fun check(item: AdaptiveBackhaulLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulLedgerTitleLengthRule(var maxLength: Int) : AdaptiveBackhaulLedgerRule {
    override fun check(item: AdaptiveBackhaulLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive backhaul ledger. */
data class AdaptiveBackhaulLedgerValidator(var rules: MutableList<AdaptiveBackhaulLedgerRule>) {
    fun validate(item: AdaptiveBackhaulLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveBackhaulLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveBackhaulLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveBackhaulLedgerValidator(): AdaptiveBackhaulLedgerValidator {
    return AdaptiveBackhaulLedgerValidator(
        mutableListOf<AdaptiveBackhaulLedgerRule>(
            AdaptiveBackhaulLedgerRequiredCarrierRule("adaptive backhaul ledger needs a value"),
            AdaptiveBackhaulLedgerBudgetRangeRule(2.5, 2776.3),
            AdaptiveBackhaulLedgerCarrierLengthRule(32),
            AdaptiveBackhaulLedgerRequiredTitleRule("adaptive backhaul ledger needs this field"),
            AdaptiveBackhaulLedgerAllowedCodeRule(mutableListOf<String>("weighted", "manual", "tiered")),
            AdaptiveBackhaulLedgerTitleLengthRule(16),
        ),
    )
}
