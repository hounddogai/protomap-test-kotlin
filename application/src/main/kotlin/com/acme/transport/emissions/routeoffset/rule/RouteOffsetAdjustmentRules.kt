package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.text.normalize

/** A rule a route offset adjustment must pass. Check returns its problems, or none. */
interface RouteOffsetAdjustmentRule {
    fun check(item: RouteOffsetAdjustment): MutableList<String>
}

data class RouteOffsetAdjustmentCodePrefixRule(var prefix: String) : RouteOffsetAdjustmentRule {
    override fun check(item: RouteOffsetAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAdjustmentAllowedTitleRule(var allowed: MutableList<String>) : RouteOffsetAdjustmentRule {
    override fun check(item: RouteOffsetAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAdjustmentScoreRangeRule(var min: Double, var max: Double) : RouteOffsetAdjustmentRule {
    override fun check(item: RouteOffsetAdjustment): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetAdjustmentRequiredNameRule(var message: String) : RouteOffsetAdjustmentRule {
    override fun check(item: RouteOffsetAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset adjustment. */
data class RouteOffsetAdjustmentValidator(var rules: MutableList<RouteOffsetAdjustmentRule>) {
    fun validate(item: RouteOffsetAdjustment): MutableList<String> {
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

    fun requireValid(item: RouteOffsetAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetAdjustmentValidator(): RouteOffsetAdjustmentValidator {
    return RouteOffsetAdjustmentValidator(
        mutableListOf<RouteOffsetAdjustmentRule>(
            RouteOffsetAdjustmentCodePrefixRule("Q"),
            RouteOffsetAdjustmentAllowedTitleRule(mutableListOf<String>("batched", "capped", "weighted")),
            RouteOffsetAdjustmentScoreRangeRule(9.9, 4210.9),
            RouteOffsetAdjustmentRequiredNameRule("route offset adjustment needs a value"),
        ),
    )
}
