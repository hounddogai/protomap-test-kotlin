package com.acme.transport.emissions.routeoffset.estimate

/** A rule a route offset estimate must pass. Check returns its problems, or none. */
interface RouteOffsetEstimateRule {
    fun check(item: RouteOffsetEstimate): MutableList<String>
}

data class RouteOffsetEstimateBatchSizeRangeRule(var min: Int, var max: Int) : RouteOffsetEstimateRule {
    override fun check(item: RouteOffsetEstimate): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetEstimateRequiredCodeRule(var message: String) : RouteOffsetEstimateRule {
    override fun check(item: RouteOffsetEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetEstimateLaneLengthRule(var maxLength: Int) : RouteOffsetEstimateRule {
    override fun check(item: RouteOffsetEstimate): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetEstimateRequiredLaneRule(var message: String) : RouteOffsetEstimateRule {
    override fun check(item: RouteOffsetEstimate): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetEstimateCategoryLengthRule(var maxLength: Int) : RouteOffsetEstimateRule {
    override fun check(item: RouteOffsetEstimate): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset estimate. */
data class RouteOffsetEstimateValidator(var rules: MutableList<RouteOffsetEstimateRule>) {
    fun validate(item: RouteOffsetEstimate): MutableList<String> {
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

    fun requireValid(item: RouteOffsetEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetEstimateValidator(): RouteOffsetEstimateValidator {
    return RouteOffsetEstimateValidator(
        mutableListOf<RouteOffsetEstimateRule>(
            RouteOffsetEstimateBatchSizeRangeRule(16, 3600),
            RouteOffsetEstimateRequiredCodeRule("route offset estimate needs a value"),
            RouteOffsetEstimateLaneLengthRule(64),
            RouteOffsetEstimateRequiredLaneRule("route offset estimate needs this field"),
            RouteOffsetEstimateCategoryLengthRule(80),
        ),
    )
}
