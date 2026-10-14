package com.acme.transport.emissions.routeoffset.slot

/** A rule a route offset target must pass. Check returns its problems, or none. */
interface RouteOffsetTargetRule {
    fun check(item: RouteOffsetTarget): MutableList<String>
}

data class RouteOffsetTargetTitleLengthRule(var maxLength: Int) : RouteOffsetTargetRule {
    override fun check(item: RouteOffsetTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetTargetSourceLengthRule(var maxLength: Int) : RouteOffsetTargetRule {
    override fun check(item: RouteOffsetTarget): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetTargetRequiredTitleRule(var message: String) : RouteOffsetTargetRule {
    override fun check(item: RouteOffsetTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetTargetRequiredVehicleIdRule(var message: String) : RouteOffsetTargetRule {
    override fun check(item: RouteOffsetTarget): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset target. */
data class RouteOffsetTargetValidator(var rules: MutableList<RouteOffsetTargetRule>) {
    fun validate(item: RouteOffsetTarget): MutableList<String> {
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

    fun requireValid(item: RouteOffsetTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetTargetValidator(): RouteOffsetTargetValidator {
    return RouteOffsetTargetValidator(
        mutableListOf<RouteOffsetTargetRule>(
            RouteOffsetTargetTitleLengthRule(1000),
            RouteOffsetTargetSourceLengthRule(16),
            RouteOffsetTargetRequiredTitleRule("route offset target needs an entry"),
            RouteOffsetTargetRequiredVehicleIdRule("route offset target needs an entry"),
        ),
    )
}
