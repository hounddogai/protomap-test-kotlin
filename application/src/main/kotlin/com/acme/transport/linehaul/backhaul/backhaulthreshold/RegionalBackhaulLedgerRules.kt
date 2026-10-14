package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.normalize

/** A rule a regional backhaul ledger must pass. Check returns its problems, or none. */
interface RegionalBackhaulLedgerRule {
    fun check(item: RegionalBackhaulLedger): MutableList<String>
}

data class RegionalBackhaulLedgerRequiredRouteCodeRule(var message: String) : RegionalBackhaulLedgerRule {
    override fun check(item: RegionalBackhaulLedger): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalBackhaulLedgerRateRangeRule(var min: Double, var max: Double) : RegionalBackhaulLedgerRule {
    override fun check(item: RegionalBackhaulLedger): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class RegionalBackhaulLedgerRequiredSegmentRule(var message: String) : RegionalBackhaulLedgerRule {
    override fun check(item: RegionalBackhaulLedger): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalBackhaulLedgerRouteCodeLengthRule(var maxLength: Int) : RegionalBackhaulLedgerRule {
    override fun check(item: RegionalBackhaulLedger): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBackhaulLedgerAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : RegionalBackhaulLedgerRule {
    override fun check(item: RegionalBackhaulLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBackhaulLedgerRequiredNameRule(var message: String) : RegionalBackhaulLedgerRule {
    override fun check(item: RegionalBackhaulLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional backhaul ledger. */
data class RegionalBackhaulLedgerValidator(var rules: MutableList<RegionalBackhaulLedgerRule>) {
    fun validate(item: RegionalBackhaulLedger): MutableList<String> {
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

    fun requireValid(item: RegionalBackhaulLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalBackhaulLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalBackhaulLedgerValidator(): RegionalBackhaulLedgerValidator {
    return RegionalBackhaulLedgerValidator(
        mutableListOf<RegionalBackhaulLedgerRule>(
            RegionalBackhaulLedgerRequiredRouteCodeRule("regional backhaul ledger needs a value"),
            RegionalBackhaulLedgerRateRangeRule(7.7, 1789.2),
            RegionalBackhaulLedgerRequiredSegmentRule("regional backhaul ledger needs an entry"),
            RegionalBackhaulLedgerRouteCodeLengthRule(120),
            RegionalBackhaulLedgerAllowedRouteCodeRule(mutableListOf<String>("primary", "scheduled", "rolling")),
            RegionalBackhaulLedgerRequiredNameRule("regional backhaul ledger needs an entry"),
        ),
    )
}
