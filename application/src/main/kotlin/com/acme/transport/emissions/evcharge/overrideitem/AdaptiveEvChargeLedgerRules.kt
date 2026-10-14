package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a adaptive ev charge ledger must pass. Check returns its problems, or none. */
interface AdaptiveEvChargeLedgerRule {
    fun check(item: AdaptiveEvChargeLedger): MutableList<String>
}

data class AdaptiveEvChargeLedgerRequiredChannelRule(var message: String) : AdaptiveEvChargeLedgerRule {
    override fun check(item: AdaptiveEvChargeLedger): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeLedgerTitleLengthRule(var maxLength: Int) : AdaptiveEvChargeLedgerRule {
    override fun check(item: AdaptiveEvChargeLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeLedgerAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : AdaptiveEvChargeLedgerRule {
    override fun check(item: AdaptiveEvChargeLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is stale")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeLedgerRequiredVehicleIdRule(var message: String) : AdaptiveEvChargeLedgerRule {
    override fun check(item: AdaptiveEvChargeLedger): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive ev charge ledger. */
data class AdaptiveEvChargeLedgerValidator(var rules: MutableList<AdaptiveEvChargeLedgerRule>) {
    fun validate(item: AdaptiveEvChargeLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveEvChargeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEvChargeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEvChargeLedgerValidator(): AdaptiveEvChargeLedgerValidator {
    return AdaptiveEvChargeLedgerValidator(
        mutableListOf<AdaptiveEvChargeLedgerRule>(
            AdaptiveEvChargeLedgerRequiredChannelRule("adaptive ev charge ledger needs an entry"),
            AdaptiveEvChargeLedgerTitleLengthRule(255),
            AdaptiveEvChargeLedgerAllowedCurrencyRule(mutableListOf<String>("strict")),
            AdaptiveEvChargeLedgerRequiredVehicleIdRule("adaptive ev charge ledger needs an entry"),
        ),
    )
}
