package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.text.normalize

/** A rule a fallback dispute ledger must pass. Check returns its problems, or none. */
interface FallbackDisputeLedgerRule {
    fun check(item: FallbackDisputeLedger): MutableList<String>
}

data class FallbackDisputeLedgerEtaMinutesRangeRule(var min: Int, var max: Int) : FallbackDisputeLedgerRule {
    override fun check(item: FallbackDisputeLedger): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeLedgerRequiredNameRule(var message: String) : FallbackDisputeLedgerRule {
    override fun check(item: FallbackDisputeLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeLedgerLoadKgRangeRule(var min: Double, var max: Double) : FallbackDisputeLedgerRule {
    override fun check(item: FallbackDisputeLedger): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class FallbackDisputeLedgerAllowedChannelRule(
    var allowed: MutableList<String>,
) : FallbackDisputeLedgerRule {
    override fun check(item: FallbackDisputeLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeLedgerRequiredCodeRule(var message: String) : FallbackDisputeLedgerRule {
    override fun check(item: FallbackDisputeLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dispute ledger. */
data class FallbackDisputeLedgerValidator(var rules: MutableList<FallbackDisputeLedgerRule>) {
    fun validate(item: FallbackDisputeLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDisputeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDisputeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDisputeLedgerValidator(): FallbackDisputeLedgerValidator {
    return FallbackDisputeLedgerValidator(
        mutableListOf<FallbackDisputeLedgerRule>(
            FallbackDisputeLedgerEtaMinutesRangeRule(64, 255),
            FallbackDisputeLedgerRequiredNameRule("fallback dispute ledger needs an entry"),
            FallbackDisputeLedgerLoadKgRangeRule(4.8, 4112.8),
            FallbackDisputeLedgerAllowedChannelRule(mutableListOf<String>("tiered")),
            FallbackDisputeLedgerRequiredCodeRule("fallback dispute ledger needs a value"),
        ),
    )
}
