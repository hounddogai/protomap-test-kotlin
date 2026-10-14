package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.text.normalize

/** A rule a default route offset ledger must pass. Check returns its problems, or none. */
interface DefaultRouteOffsetLedgerRule {
    fun check(item: DefaultRouteOffsetLedger): MutableList<String>
}

data class DefaultRouteOffsetLedgerAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : DefaultRouteOffsetLedgerRule {
    override fun check(item: DefaultRouteOffsetLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRouteOffsetLedgerRequiredCodeRule(var message: String) : DefaultRouteOffsetLedgerRule {
    override fun check(item: DefaultRouteOffsetLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRouteOffsetLedgerCodeLengthRule(var maxLength: Int) : DefaultRouteOffsetLedgerRule {
    override fun check(item: DefaultRouteOffsetLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRouteOffsetLedgerMaximumRangeRule(
    var min: Double,
    var max: Double,
) : DefaultRouteOffsetLedgerRule {
    override fun check(item: DefaultRouteOffsetLedger): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default route offset ledger. */
data class DefaultRouteOffsetLedgerValidator(var rules: MutableList<DefaultRouteOffsetLedgerRule>) {
    fun validate(item: DefaultRouteOffsetLedger): MutableList<String> {
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

    fun requireValid(item: DefaultRouteOffsetLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRouteOffsetLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRouteOffsetLedgerValidator(): DefaultRouteOffsetLedgerValidator {
    return DefaultRouteOffsetLedgerValidator(
        mutableListOf<DefaultRouteOffsetLedgerRule>(
            DefaultRouteOffsetLedgerAllowedVehicleIdRule(mutableListOf<String>("default", "weighted")),
            DefaultRouteOffsetLedgerRequiredCodeRule("default route offset ledger needs this field"),
            DefaultRouteOffsetLedgerCodeLengthRule(80),
            DefaultRouteOffsetLedgerMaximumRangeRule(7.8, 1226.1),
        ),
    )
}
