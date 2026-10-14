package com.acme.transport.emissions.routeoffset.window

/** A rule a route offset variance must pass. Check returns its problems, or none. */
interface RouteOffsetVarianceRule {
    fun check(item: RouteOffsetVariance): MutableList<String>
}

data class RouteOffsetVariancePinnedGuardRule(var limit: Double) : RouteOffsetVarianceRule {
    override fun check(item: RouteOffsetVariance): MutableList<String> {
        if (item.pinned && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetVarianceTimezoneLengthRule(var maxLength: Int) : RouteOffsetVarianceRule {
    override fun check(item: RouteOffsetVariance): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetVarianceRequiredCodeRule(var message: String) : RouteOffsetVarianceRule {
    override fun check(item: RouteOffsetVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetVarianceNameLengthRule(var maxLength: Int) : RouteOffsetVarianceRule {
    override fun check(item: RouteOffsetVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetVarianceCodePrefixRule(var prefix: String) : RouteOffsetVarianceRule {
    override fun check(item: RouteOffsetVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetVarianceRouteCodeLengthRule(var maxLength: Int) : RouteOffsetVarianceRule {
    override fun check(item: RouteOffsetVariance): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset variance. */
data class RouteOffsetVarianceValidator(var rules: MutableList<RouteOffsetVarianceRule>) {
    fun validate(item: RouteOffsetVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetVarianceValidator(): RouteOffsetVarianceValidator {
    return RouteOffsetVarianceValidator(
        mutableListOf<RouteOffsetVarianceRule>(
            RouteOffsetVariancePinnedGuardRule(9.6),
            RouteOffsetVarianceTimezoneLengthRule(86400),
            RouteOffsetVarianceRequiredCodeRule("route offset variance needs this field"),
            RouteOffsetVarianceNameLengthRule(1000),
            RouteOffsetVarianceCodePrefixRule("X"),
            RouteOffsetVarianceRouteCodeLengthRule(120),
        ),
    )
}
