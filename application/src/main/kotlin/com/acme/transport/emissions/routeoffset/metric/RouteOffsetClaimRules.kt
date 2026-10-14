package com.acme.transport.emissions.routeoffset.metric

/** A rule a route offset claim must pass. Check returns its problems, or none. */
interface RouteOffsetClaimRule {
    fun check(item: RouteOffsetClaim): MutableList<String>
}

data class RouteOffsetClaimRequiredTrailerIdRule(var message: String) : RouteOffsetClaimRule {
    override fun check(item: RouteOffsetClaim): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetClaimTrailerIdLengthRule(var maxLength: Int) : RouteOffsetClaimRule {
    override fun check(item: RouteOffsetClaim): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetClaimCreatedAtRangeRule(var min: Int, var max: Int) : RouteOffsetClaimRule {
    override fun check(item: RouteOffsetClaim): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset claim. */
data class RouteOffsetClaimValidator(var rules: MutableList<RouteOffsetClaimRule>) {
    fun validate(item: RouteOffsetClaim): MutableList<String> {
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

    fun requireValid(item: RouteOffsetClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetClaimValidator(): RouteOffsetClaimValidator {
    return RouteOffsetClaimValidator(
        mutableListOf<RouteOffsetClaimRule>(
            RouteOffsetClaimRequiredTrailerIdRule("route offset claim needs this field"),
            RouteOffsetClaimTrailerIdLengthRule(80),
            RouteOffsetClaimCreatedAtRangeRule(120, 255),
        ),
    )
}
