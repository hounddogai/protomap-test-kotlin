package com.acme.transport.emissions.routeoffset.summary

/** A rule a route offset allocation must pass. Check returns its problems, or none. */
interface RouteOffsetAllocationRule {
    fun check(item: RouteOffsetAllocation): MutableList<String>
}

data class RouteOffsetAllocationCodePrefixRule(var prefix: String) : RouteOffsetAllocationRule {
    override fun check(item: RouteOffsetAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAllocationRequiredCodeRule(var message: String) : RouteOffsetAllocationRule {
    override fun check(item: RouteOffsetAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAllocationCodeLengthRule(var maxLength: Int) : RouteOffsetAllocationRule {
    override fun check(item: RouteOffsetAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAllocationWindowDaysRangeRule(var min: Int, var max: Int) : RouteOffsetAllocationRule {
    override fun check(item: RouteOffsetAllocation): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset allocation. */
data class RouteOffsetAllocationValidator(var rules: MutableList<RouteOffsetAllocationRule>) {
    fun validate(item: RouteOffsetAllocation): MutableList<String> {
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

    fun requireValid(item: RouteOffsetAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetAllocationValidator(): RouteOffsetAllocationValidator {
    return RouteOffsetAllocationValidator(
        mutableListOf<RouteOffsetAllocationRule>(
            RouteOffsetAllocationCodePrefixRule("ACM"),
            RouteOffsetAllocationRequiredCodeRule("route offset allocation needs a value"),
            RouteOffsetAllocationCodeLengthRule(16),
            RouteOffsetAllocationWindowDaysRangeRule(16, 120),
        ),
    )
}
