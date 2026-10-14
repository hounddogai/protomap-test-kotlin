package com.acme.transport.emissions.routeoffset.assignment

/** A rule a route offset tier must pass. Check returns its problems, or none. */
interface RouteOffsetTierRule {
    fun check(item: RouteOffsetTier): MutableList<String>
}

data class RouteOffsetTierRequiredCodeRule(var message: String) : RouteOffsetTierRule {
    override fun check(item: RouteOffsetTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetTierNameLengthRule(var maxLength: Int) : RouteOffsetTierRule {
    override fun check(item: RouteOffsetTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetTierEndsAtRangeRule(var min: Int, var max: Int) : RouteOffsetTierRule {
    override fun check(item: RouteOffsetTier): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetTierMaximumRangeRule(var min: Double, var max: Double) : RouteOffsetTierRule {
    override fun check(item: RouteOffsetTier): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetTierCodePrefixRule(var prefix: String) : RouteOffsetTierRule {
    override fun check(item: RouteOffsetTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetTierCategoryLengthRule(var maxLength: Int) : RouteOffsetTierRule {
    override fun check(item: RouteOffsetTier): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset tier. */
data class RouteOffsetTierValidator(var rules: MutableList<RouteOffsetTierRule>) {
    fun validate(item: RouteOffsetTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetTierValidator(): RouteOffsetTierValidator {
    return RouteOffsetTierValidator(
        mutableListOf<RouteOffsetTierRule>(
            RouteOffsetTierRequiredCodeRule("route offset tier needs an entry"),
            RouteOffsetTierNameLengthRule(64),
            RouteOffsetTierEndsAtRangeRule(32, 120),
            RouteOffsetTierMaximumRangeRule(5.3, 4041.2),
            RouteOffsetTierCodePrefixRule("OPS"),
            RouteOffsetTierCategoryLengthRule(3600),
        ),
    )
}
