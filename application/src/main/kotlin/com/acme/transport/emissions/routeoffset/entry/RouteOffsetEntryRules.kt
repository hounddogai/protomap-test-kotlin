package com.acme.transport.emissions.routeoffset.entry

/** A rule a route offset entry must pass. Check returns its problems, or none. */
interface RouteOffsetEntryRule {
    fun check(item: RouteOffsetEntry): MutableList<String>
}

data class RouteOffsetEntryRequiredCodeRule(var message: String) : RouteOffsetEntryRule {
    override fun check(item: RouteOffsetEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetEntryRequiredLabelRule(var message: String) : RouteOffsetEntryRule {
    override fun check(item: RouteOffsetEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetEntryCodePrefixRule(var prefix: String) : RouteOffsetEntryRule {
    override fun check(item: RouteOffsetEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset entry. */
data class RouteOffsetEntryValidator(var rules: MutableList<RouteOffsetEntryRule>) {
    fun validate(item: RouteOffsetEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetEntryValidator(): RouteOffsetEntryValidator {
    return RouteOffsetEntryValidator(
        mutableListOf<RouteOffsetEntryRule>(
            RouteOffsetEntryRequiredCodeRule("route offset entry needs an entry"),
            RouteOffsetEntryRequiredLabelRule("route offset entry needs an entry"),
            RouteOffsetEntryCodePrefixRule("Q"),
        ),
    )
}
