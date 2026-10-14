package com.acme.transport.emissions.routeoffset.rollup

/** A rule a route offset rollup must pass. Check returns its problems, or none. */
interface RouteOffsetRollupRule {
    fun check(item: RouteOffsetRollup): MutableList<String>
}

data class RouteOffsetRollupCodePrefixRule(var prefix: String) : RouteOffsetRollupRule {
    override fun check(item: RouteOffsetRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetRollupRequiredSegmentRule(var message: String) : RouteOffsetRollupRule {
    override fun check(item: RouteOffsetRollup): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetRollupCostRangeRule(var min: Double, var max: Double) : RouteOffsetRollupRule {
    override fun check(item: RouteOffsetRollup): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetRollupSegmentLengthRule(var maxLength: Int) : RouteOffsetRollupRule {
    override fun check(item: RouteOffsetRollup): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset rollup. */
data class RouteOffsetRollupValidator(var rules: MutableList<RouteOffsetRollupRule>) {
    fun validate(item: RouteOffsetRollup): MutableList<String> {
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

    fun requireValid(item: RouteOffsetRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetRollupValidator(): RouteOffsetRollupValidator {
    return RouteOffsetRollupValidator(
        mutableListOf<RouteOffsetRollupRule>(
            RouteOffsetRollupCodePrefixRule("X"),
            RouteOffsetRollupRequiredSegmentRule("route offset rollup needs a value"),
            RouteOffsetRollupCostRangeRule(9.8, 3255.9),
            RouteOffsetRollupSegmentLengthRule(64),
        ),
    )
}
