package com.acme.transport.emissions.routeoffset.summary

/** A rule a route offset baseline must pass. Check returns its problems, or none. */
interface RouteOffsetBaselineRule {
    fun check(item: RouteOffsetBaseline): MutableList<String>
}

data class RouteOffsetBaselineLocaleLengthRule(var maxLength: Int) : RouteOffsetBaselineRule {
    override fun check(item: RouteOffsetBaseline): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetBaselineRevisionRangeRule(var min: Int, var max: Int) : RouteOffsetBaselineRule {
    override fun check(item: RouteOffsetBaseline): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetBaselineGraceDaysRangeRule(var min: Int, var max: Int) : RouteOffsetBaselineRule {
    override fun check(item: RouteOffsetBaseline): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetBaselineCodePrefixRule(var prefix: String) : RouteOffsetBaselineRule {
    override fun check(item: RouteOffsetBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetBaselineSequenceRangeRule(var min: Int, var max: Int) : RouteOffsetBaselineRule {
    override fun check(item: RouteOffsetBaseline): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetBaselineMarginRangeRule(var min: Double, var max: Double) : RouteOffsetBaselineRule {
    override fun check(item: RouteOffsetBaseline): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset baseline. */
data class RouteOffsetBaselineValidator(var rules: MutableList<RouteOffsetBaselineRule>) {
    fun validate(item: RouteOffsetBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetBaselineValidator(): RouteOffsetBaselineValidator {
    return RouteOffsetBaselineValidator(
        mutableListOf<RouteOffsetBaselineRule>(
            RouteOffsetBaselineLocaleLengthRule(86400),
            RouteOffsetBaselineRevisionRangeRule(255, 3600),
            RouteOffsetBaselineGraceDaysRangeRule(3600, 86400),
            RouteOffsetBaselineCodePrefixRule("ACM"),
            RouteOffsetBaselineSequenceRangeRule(255, 3600),
            RouteOffsetBaselineMarginRangeRule(6.7, 3591.6),
        ),
    )
}
