package com.acme.transport.emissions.routeoffset.offsetmapping

/** A rule a route offset digest must pass. Check returns its problems, or none. */
interface RouteOffsetDigestRule {
    fun check(item: RouteOffsetDigest): MutableList<String>
}

data class RouteOffsetDigestPriorityRangeRule(var min: Int, var max: Int) : RouteOffsetDigestRule {
    override fun check(item: RouteOffsetDigest): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetDigestCodeLengthRule(var maxLength: Int) : RouteOffsetDigestRule {
    override fun check(item: RouteOffsetDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetDigestStopCountRangeRule(var min: Int, var max: Int) : RouteOffsetDigestRule {
    override fun check(item: RouteOffsetDigest): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset digest. */
data class RouteOffsetDigestValidator(var rules: MutableList<RouteOffsetDigestRule>) {
    fun validate(item: RouteOffsetDigest): MutableList<String> {
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

    fun requireValid(item: RouteOffsetDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetDigestValidator(): RouteOffsetDigestValidator {
    return RouteOffsetDigestValidator(
        mutableListOf<RouteOffsetDigestRule>(
            RouteOffsetDigestPriorityRangeRule(3600, 86400),
            RouteOffsetDigestCodeLengthRule(64),
            RouteOffsetDigestStopCountRangeRule(80, 1000),
        ),
    )
}
