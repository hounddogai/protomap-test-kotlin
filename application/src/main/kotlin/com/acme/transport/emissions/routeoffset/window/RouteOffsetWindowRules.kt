package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.text.normalize

/** A rule a route offset window must pass. Check returns its problems, or none. */
interface RouteOffsetWindowRule {
    fun check(item: RouteOffsetWindow): MutableList<String>
}

data class RouteOffsetWindowAllowedContactEmailRule(var allowed: MutableList<String>) : RouteOffsetWindowRule {
    override fun check(item: RouteOffsetWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetWindowRequiredTitleRule(var message: String) : RouteOffsetWindowRule {
    override fun check(item: RouteOffsetWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetWindowRequiredCodeRule(var message: String) : RouteOffsetWindowRule {
    override fun check(item: RouteOffsetWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset window. */
data class RouteOffsetWindowValidator(var rules: MutableList<RouteOffsetWindowRule>) {
    fun validate(item: RouteOffsetWindow): MutableList<String> {
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

    fun requireValid(item: RouteOffsetWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetWindowValidator(): RouteOffsetWindowValidator {
    return RouteOffsetWindowValidator(
        mutableListOf<RouteOffsetWindowRule>(
            RouteOffsetWindowAllowedContactEmailRule(mutableListOf<String>("fallback", "regional", "regional")),
            RouteOffsetWindowRequiredTitleRule("route offset window needs this field"),
            RouteOffsetWindowRequiredCodeRule("route offset window needs this field"),
        ),
    )
}
