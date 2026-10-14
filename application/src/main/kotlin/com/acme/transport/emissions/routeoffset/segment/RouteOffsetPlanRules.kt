package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.text.normalize

/** A rule a route offset plan must pass. Check returns its problems, or none. */
interface RouteOffsetPlanRule {
    fun check(item: RouteOffsetPlan): MutableList<String>
}

data class RouteOffsetPlanRequiredChannelRule(var message: String) : RouteOffsetPlanRule {
    override fun check(item: RouteOffsetPlan): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetPlanRouteCodeLengthRule(var maxLength: Int) : RouteOffsetPlanRule {
    override fun check(item: RouteOffsetPlan): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetPlanAllowedNameRule(var allowed: MutableList<String>) : RouteOffsetPlanRule {
    override fun check(item: RouteOffsetPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetPlanCodeLengthRule(var maxLength: Int) : RouteOffsetPlanRule {
    override fun check(item: RouteOffsetPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset plan. */
data class RouteOffsetPlanValidator(var rules: MutableList<RouteOffsetPlanRule>) {
    fun validate(item: RouteOffsetPlan): MutableList<String> {
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

    fun requireValid(item: RouteOffsetPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetPlanValidator(): RouteOffsetPlanValidator {
    return RouteOffsetPlanValidator(
        mutableListOf<RouteOffsetPlanRule>(
            RouteOffsetPlanRequiredChannelRule("route offset plan needs this field"),
            RouteOffsetPlanRouteCodeLengthRule(120),
            RouteOffsetPlanAllowedNameRule(mutableListOf<String>("fallback")),
            RouteOffsetPlanCodeLengthRule(32),
        ),
    )
}
