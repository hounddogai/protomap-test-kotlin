package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.text.normalize

/** A rule a global transit time ledger must pass. Check returns its problems, or none. */
interface GlobalTransitTimeLedgerRule {
    fun check(item: GlobalTransitTimeLedger): MutableList<String>
}

data class GlobalTransitTimeLedgerRequiredNameRule(var message: String) : GlobalTransitTimeLedgerRule {
    override fun check(item: GlobalTransitTimeLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeLedgerVehicleIdLengthRule(var maxLength: Int) : GlobalTransitTimeLedgerRule {
    override fun check(item: GlobalTransitTimeLedger): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeLedgerAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : GlobalTransitTimeLedgerRule {
    override fun check(item: GlobalTransitTimeLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeLedgerNameLengthRule(var maxLength: Int) : GlobalTransitTimeLedgerRule {
    override fun check(item: GlobalTransitTimeLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeLedgerCodePrefixRule(var prefix: String) : GlobalTransitTimeLedgerRule {
    override fun check(item: GlobalTransitTimeLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global transit time ledger. */
data class GlobalTransitTimeLedgerValidator(var rules: MutableList<GlobalTransitTimeLedgerRule>) {
    fun validate(item: GlobalTransitTimeLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTransitTimeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTransitTimeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTransitTimeLedgerValidator(): GlobalTransitTimeLedgerValidator {
    return GlobalTransitTimeLedgerValidator(
        mutableListOf<GlobalTransitTimeLedgerRule>(
            GlobalTransitTimeLedgerRequiredNameRule("global transit time ledger needs an entry"),
            GlobalTransitTimeLedgerVehicleIdLengthRule(255),
            GlobalTransitTimeLedgerAllowedRouteCodeRule(mutableListOf<String>()),
            GlobalTransitTimeLedgerNameLengthRule(255),
            GlobalTransitTimeLedgerCodePrefixRule("OPS"),
        ),
    )
}
