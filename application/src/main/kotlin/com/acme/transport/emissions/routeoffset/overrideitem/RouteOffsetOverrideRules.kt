package com.acme.transport.emissions.routeoffset.overrideitem

/** A rule a route offset override must pass. Check returns its problems, or none. */
interface RouteOffsetOverrideRule {
    fun check(item: RouteOffsetOverride): MutableList<String>
}

data class RouteOffsetOverrideRequiredTitleRule(var message: String) : RouteOffsetOverrideRule {
    override fun check(item: RouteOffsetOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetOverrideCodePrefixRule(var prefix: String) : RouteOffsetOverrideRule {
    override fun check(item: RouteOffsetOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetOverrideTitleLengthRule(var maxLength: Int) : RouteOffsetOverrideRule {
    override fun check(item: RouteOffsetOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetOverrideMarginRangeRule(var min: Double, var max: Double) : RouteOffsetOverrideRule {
    override fun check(item: RouteOffsetOverride): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset override. */
data class RouteOffsetOverrideValidator(var rules: MutableList<RouteOffsetOverrideRule>) {
    fun validate(item: RouteOffsetOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetOverrideValidator(): RouteOffsetOverrideValidator {
    return RouteOffsetOverrideValidator(
        mutableListOf<RouteOffsetOverrideRule>(
            RouteOffsetOverrideRequiredTitleRule("route offset override needs this field"),
            RouteOffsetOverrideCodePrefixRule("X"),
            RouteOffsetOverrideTitleLengthRule(255),
            RouteOffsetOverrideMarginRangeRule(4.4, 601.3),
        ),
    )
}
