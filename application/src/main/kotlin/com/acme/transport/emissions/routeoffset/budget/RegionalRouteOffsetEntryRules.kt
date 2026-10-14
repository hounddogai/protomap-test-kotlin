package com.acme.transport.emissions.routeoffset.budget

/** A rule a regional route offset entry must pass. Check returns its problems, or none. */
interface RegionalRouteOffsetEntryRule {
    fun check(item: RegionalRouteOffsetEntry): MutableList<String>
}

data class RegionalRouteOffsetEntryRequiredCodeRule(var message: String) : RegionalRouteOffsetEntryRule {
    override fun check(item: RegionalRouteOffsetEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRouteOffsetEntryFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RegionalRouteOffsetEntryRule {
    override fun check(item: RegionalRouteOffsetEntry): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class RegionalRouteOffsetEntryRequiredTrailerIdRule(var message: String) : RegionalRouteOffsetEntryRule {
    override fun check(item: RegionalRouteOffsetEntry): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRouteOffsetEntryCodePrefixRule(var prefix: String) : RegionalRouteOffsetEntryRule {
    override fun check(item: RegionalRouteOffsetEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional route offset entry. */
data class RegionalRouteOffsetEntryValidator(var rules: MutableList<RegionalRouteOffsetEntryRule>) {
    fun validate(item: RegionalRouteOffsetEntry): MutableList<String> {
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

    fun requireValid(item: RegionalRouteOffsetEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRouteOffsetEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRouteOffsetEntryValidator(): RegionalRouteOffsetEntryValidator {
    return RegionalRouteOffsetEntryValidator(
        mutableListOf<RegionalRouteOffsetEntryRule>(
            RegionalRouteOffsetEntryRequiredCodeRule("regional route offset entry needs an entry"),
            RegionalRouteOffsetEntryFuelLitersRangeRule(8.7, 4058.3),
            RegionalRouteOffsetEntryRequiredTrailerIdRule("regional route offset entry needs a value"),
            RegionalRouteOffsetEntryCodePrefixRule("ACM"),
        ),
    )
}
