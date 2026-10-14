package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.text.normalize

/** A rule a route offset group must pass. Check returns its problems, or none. */
interface RouteOffsetGroupRule {
    fun check(item: RouteOffsetGroup): MutableList<String>
}

data class RouteOffsetGroupCodePrefixRule(var prefix: String) : RouteOffsetGroupRule {
    override fun check(item: RouteOffsetGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetGroupAllowedTitleRule(var allowed: MutableList<String>) : RouteOffsetGroupRule {
    override fun check(item: RouteOffsetGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetGroupRetriesRangeRule(var min: Int, var max: Int) : RouteOffsetGroupRule {
    override fun check(item: RouteOffsetGroup): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetGroupLoadKgRangeRule(var min: Double, var max: Double) : RouteOffsetGroupRule {
    override fun check(item: RouteOffsetGroup): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetGroupMarginRangeRule(var min: Double, var max: Double) : RouteOffsetGroupRule {
    override fun check(item: RouteOffsetGroup): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a route offset group. */
data class RouteOffsetGroupValidator(var rules: MutableList<RouteOffsetGroupRule>) {
    fun validate(item: RouteOffsetGroup): MutableList<String> {
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

    fun requireValid(item: RouteOffsetGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetGroupValidator(): RouteOffsetGroupValidator {
    return RouteOffsetGroupValidator(
        mutableListOf<RouteOffsetGroupRule>(
            RouteOffsetGroupCodePrefixRule("ACM"),
            RouteOffsetGroupAllowedTitleRule(mutableListOf<String>("weighted", "weighted")),
            RouteOffsetGroupRetriesRangeRule(16, 3600),
            RouteOffsetGroupLoadKgRangeRule(1.2, 4247.6),
            RouteOffsetGroupMarginRangeRule(4.6, 370.1),
        ),
    )
}
