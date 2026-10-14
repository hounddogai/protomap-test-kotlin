package com.acme.transport.emissions.routeoffset.rule

/** A rule a route offset segment must pass. Check returns its problems, or none. */
interface RouteOffsetSegmentRule {
    fun check(item: RouteOffsetSegment): MutableList<String>
}

data class RouteOffsetSegmentCodeLengthRule(var maxLength: Int) : RouteOffsetSegmentRule {
    override fun check(item: RouteOffsetSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSegmentStopCountRangeRule(var min: Int, var max: Int) : RouteOffsetSegmentRule {
    override fun check(item: RouteOffsetSegment): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetSegmentRequiredNameRule(var message: String) : RouteOffsetSegmentRule {
    override fun check(item: RouteOffsetSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset segment. */
data class RouteOffsetSegmentValidator(var rules: MutableList<RouteOffsetSegmentRule>) {
    fun validate(item: RouteOffsetSegment): MutableList<String> {
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

    fun requireValid(item: RouteOffsetSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetSegmentValidator(): RouteOffsetSegmentValidator {
    return RouteOffsetSegmentValidator(
        mutableListOf<RouteOffsetSegmentRule>(
            RouteOffsetSegmentCodeLengthRule(255),
            RouteOffsetSegmentStopCountRangeRule(64, 255),
            RouteOffsetSegmentRequiredNameRule("route offset segment needs this field"),
        ),
    )
}
