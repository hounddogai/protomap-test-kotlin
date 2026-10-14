package com.acme.transport.emissions.routeoffset.profile

/** A rule a route offset ledger must pass. Check returns its problems, or none. */
interface RouteOffsetLedgerRule {
    fun check(item: RouteOffsetLedger): MutableList<String>
}

data class RouteOffsetLedgerCodeLengthRule(var maxLength: Int) : RouteOffsetLedgerRule {
    override fun check(item: RouteOffsetLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetLedgerChannelLengthRule(var maxLength: Int) : RouteOffsetLedgerRule {
    override fun check(item: RouteOffsetLedger): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetLedgerLoadKgRangeRule(var min: Double, var max: Double) : RouteOffsetLedgerRule {
    override fun check(item: RouteOffsetLedger): MutableList<String> {
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

data class RouteOffsetLedgerRequiredTitleRule(var message: String) : RouteOffsetLedgerRule {
    override fun check(item: RouteOffsetLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset ledger. */
data class RouteOffsetLedgerValidator(var rules: MutableList<RouteOffsetLedgerRule>) {
    fun validate(item: RouteOffsetLedger): MutableList<String> {
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

    fun requireValid(item: RouteOffsetLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetLedgerValidator(): RouteOffsetLedgerValidator {
    return RouteOffsetLedgerValidator(
        mutableListOf<RouteOffsetLedgerRule>(
            RouteOffsetLedgerCodeLengthRule(255),
            RouteOffsetLedgerChannelLengthRule(64),
            RouteOffsetLedgerLoadKgRangeRule(8.3, 3125.2),
            RouteOffsetLedgerRequiredTitleRule("route offset ledger needs a value"),
        ),
    )
}
