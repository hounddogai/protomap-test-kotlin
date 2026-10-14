package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.text.normalize

/** A rule a route offset checkpoint must pass. Check returns its problems, or none. */
interface RouteOffsetCheckpointRule {
    fun check(item: RouteOffsetCheckpoint): MutableList<String>
}

data class RouteOffsetCheckpointCodeLengthRule(var maxLength: Int) : RouteOffsetCheckpointRule {
    override fun check(item: RouteOffsetCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetCheckpointAllowedPhoneRule(var allowed: MutableList<String>) : RouteOffsetCheckpointRule {
    override fun check(item: RouteOffsetCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetCheckpointExpiresAtRangeRule(var min: Int, var max: Int) : RouteOffsetCheckpointRule {
    override fun check(item: RouteOffsetCheckpoint): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a route offset checkpoint. */
data class RouteOffsetCheckpointValidator(var rules: MutableList<RouteOffsetCheckpointRule>) {
    fun validate(item: RouteOffsetCheckpoint): MutableList<String> {
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

    fun requireValid(item: RouteOffsetCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetCheckpointValidator(): RouteOffsetCheckpointValidator {
    return RouteOffsetCheckpointValidator(
        mutableListOf<RouteOffsetCheckpointRule>(
            RouteOffsetCheckpointCodeLengthRule(32),
            RouteOffsetCheckpointAllowedPhoneRule(mutableListOf<String>()),
            RouteOffsetCheckpointExpiresAtRangeRule(64, 255),
        ),
    )
}
