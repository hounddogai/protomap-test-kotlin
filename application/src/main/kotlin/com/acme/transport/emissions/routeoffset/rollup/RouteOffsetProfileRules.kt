package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.kit.text.normalize

/** A rule a route offset profile must pass. Check returns its problems, or none. */
interface RouteOffsetProfileRule {
    fun check(item: RouteOffsetProfile): MutableList<String>
}

data class RouteOffsetProfileAllowedCodeRule(var allowed: MutableList<String>) : RouteOffsetProfileRule {
    override fun check(item: RouteOffsetProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetProfileTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RouteOffsetProfileRule {
    override fun check(item: RouteOffsetProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RouteOffsetProfileUpdatedAtRangeRule(var min: Int, var max: Int) : RouteOffsetProfileRule {
    override fun check(item: RouteOffsetProfile): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetProfileOdometerRangeRule(var min: Int, var max: Int) : RouteOffsetProfileRule {
    override fun check(item: RouteOffsetProfile): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetProfileRequiredCodeRule(var message: String) : RouteOffsetProfileRule {
    override fun check(item: RouteOffsetProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset profile. */
data class RouteOffsetProfileValidator(var rules: MutableList<RouteOffsetProfileRule>) {
    fun validate(item: RouteOffsetProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetProfileValidator(): RouteOffsetProfileValidator {
    return RouteOffsetProfileValidator(
        mutableListOf<RouteOffsetProfileRule>(
            RouteOffsetProfileAllowedCodeRule(mutableListOf<String>()),
            RouteOffsetProfileTagsLimitRule(255, mutableListOf<String>()),
            RouteOffsetProfileUpdatedAtRangeRule(120, 1000),
            RouteOffsetProfileOdometerRangeRule(80, 1000),
            RouteOffsetProfileRequiredCodeRule("route offset profile needs a value"),
        ),
    )
}
