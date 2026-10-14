package com.acme.transport.emissions.routeoffset.audit

/** A rule a route offset cycle must pass. Check returns its problems, or none. */
interface RouteOffsetCycleRule {
    fun check(item: RouteOffsetCycle): MutableList<String>
}

data class RouteOffsetCycleRequiredRouteCodeRule(var message: String) : RouteOffsetCycleRule {
    override fun check(item: RouteOffsetCycle): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetCycleNameLengthRule(var maxLength: Int) : RouteOffsetCycleRule {
    override fun check(item: RouteOffsetCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetCycleRevisionRangeRule(var min: Int, var max: Int) : RouteOffsetCycleRule {
    override fun check(item: RouteOffsetCycle): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetCycleRouteCodeLengthRule(var maxLength: Int) : RouteOffsetCycleRule {
    override fun check(item: RouteOffsetCycle): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetCycleFloorValueRangeRule(var min: Double, var max: Double) : RouteOffsetCycleRule {
    override fun check(item: RouteOffsetCycle): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetCycleCodePrefixRule(var prefix: String) : RouteOffsetCycleRule {
    override fun check(item: RouteOffsetCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset cycle. */
data class RouteOffsetCycleValidator(var rules: MutableList<RouteOffsetCycleRule>) {
    fun validate(item: RouteOffsetCycle): MutableList<String> {
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

    fun requireValid(item: RouteOffsetCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetCycleValidator(): RouteOffsetCycleValidator {
    return RouteOffsetCycleValidator(
        mutableListOf<RouteOffsetCycleRule>(
            RouteOffsetCycleRequiredRouteCodeRule("route offset cycle needs a value"),
            RouteOffsetCycleNameLengthRule(3600),
            RouteOffsetCycleRevisionRangeRule(32, 1000),
            RouteOffsetCycleRouteCodeLengthRule(86400),
            RouteOffsetCycleFloorValueRangeRule(2.0, 3841.3),
            RouteOffsetCycleCodePrefixRule("ACM"),
        ),
    )
}
