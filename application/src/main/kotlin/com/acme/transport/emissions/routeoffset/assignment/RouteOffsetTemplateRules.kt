package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.text.normalize

/** A rule a route offset template must pass. Check returns its problems, or none. */
interface RouteOffsetTemplateRule {
    fun check(item: RouteOffsetTemplate): MutableList<String>
}

data class RouteOffsetTemplateRequiredCodeRule(var message: String) : RouteOffsetTemplateRule {
    override fun check(item: RouteOffsetTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetTemplateAllowedNameRule(var allowed: MutableList<String>) : RouteOffsetTemplateRule {
    override fun check(item: RouteOffsetTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetTemplateVisibleGuardRule(var limit: Int) : RouteOffsetTemplateRule {
    override fun check(item: RouteOffsetTemplate): MutableList<String> {
        if (item.visible && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetTemplateFuelLitersRangeRule(var min: Double, var max: Double) : RouteOffsetTemplateRule {
    override fun check(item: RouteOffsetTemplate): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetTemplateRequiredNameRule(var message: String) : RouteOffsetTemplateRule {
    override fun check(item: RouteOffsetTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetTemplateRetriesRangeRule(var min: Int, var max: Int) : RouteOffsetTemplateRule {
    override fun check(item: RouteOffsetTemplate): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a route offset template. */
data class RouteOffsetTemplateValidator(var rules: MutableList<RouteOffsetTemplateRule>) {
    fun validate(item: RouteOffsetTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetTemplateValidator(): RouteOffsetTemplateValidator {
    return RouteOffsetTemplateValidator(
        mutableListOf<RouteOffsetTemplateRule>(
            RouteOffsetTemplateRequiredCodeRule("route offset template needs a value"),
            RouteOffsetTemplateAllowedNameRule(mutableListOf<String>("primary")),
            RouteOffsetTemplateVisibleGuardRule(16),
            RouteOffsetTemplateFuelLitersRangeRule(1.1, 1227.4),
            RouteOffsetTemplateRequiredNameRule("route offset template needs a value"),
            RouteOffsetTemplateRetriesRangeRule(255, 86400),
        ),
    )
}
