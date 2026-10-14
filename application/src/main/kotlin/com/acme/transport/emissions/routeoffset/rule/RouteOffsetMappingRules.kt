package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.text.normalize

/** A rule a route offset mapping must pass. Check returns its problems, or none. */
interface RouteOffsetMappingRule {
    fun check(item: RouteOffsetMapping): MutableList<String>
}

data class RouteOffsetMappingAllowedCodeRule(var allowed: MutableList<String>) : RouteOffsetMappingRule {
    override fun check(item: RouteOffsetMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetMappingFuelLitersRangeRule(var min: Double, var max: Double) : RouteOffsetMappingRule {
    override fun check(item: RouteOffsetMapping): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetMappingThresholdRangeRule(var min: Double, var max: Double) : RouteOffsetMappingRule {
    override fun check(item: RouteOffsetMapping): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetMappingOdometerRangeRule(var min: Int, var max: Int) : RouteOffsetMappingRule {
    override fun check(item: RouteOffsetMapping): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a route offset mapping. */
data class RouteOffsetMappingValidator(var rules: MutableList<RouteOffsetMappingRule>) {
    fun validate(item: RouteOffsetMapping): MutableList<String> {
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

    fun requireValid(item: RouteOffsetMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetMappingValidator(): RouteOffsetMappingValidator {
    return RouteOffsetMappingValidator(
        mutableListOf<RouteOffsetMappingRule>(
            RouteOffsetMappingAllowedCodeRule(mutableListOf<String>("global")),
            RouteOffsetMappingFuelLitersRangeRule(0.7, 79.1),
            RouteOffsetMappingThresholdRangeRule(2.2, 279.6),
            RouteOffsetMappingOdometerRangeRule(255, 255),
        ),
    )
}
