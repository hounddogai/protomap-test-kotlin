package com.acme.transport.emissions.routeoffset.rollup

/** A rule a route offset request must pass. Check returns its problems, or none. */
interface RouteOffsetRequestRule {
    fun check(item: RouteOffsetRequest): MutableList<String>
}

data class RouteOffsetRequestRequiredLaneRule(var message: String) : RouteOffsetRequestRule {
    override fun check(item: RouteOffsetRequest): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetRequestTitleLengthRule(var maxLength: Int) : RouteOffsetRequestRule {
    override fun check(item: RouteOffsetRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetRequestStopCountRangeRule(var min: Int, var max: Int) : RouteOffsetRequestRule {
    override fun check(item: RouteOffsetRequest): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetRequestChannelLengthRule(var maxLength: Int) : RouteOffsetRequestRule {
    override fun check(item: RouteOffsetRequest): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetRequestRetriesRangeRule(var min: Int, var max: Int) : RouteOffsetRequestRule {
    override fun check(item: RouteOffsetRequest): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset request. */
data class RouteOffsetRequestValidator(var rules: MutableList<RouteOffsetRequestRule>) {
    fun validate(item: RouteOffsetRequest): MutableList<String> {
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

    fun requireValid(item: RouteOffsetRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetRequestValidator(): RouteOffsetRequestValidator {
    return RouteOffsetRequestValidator(
        mutableListOf<RouteOffsetRequestRule>(
            RouteOffsetRequestRequiredLaneRule("route offset request needs a value"),
            RouteOffsetRequestTitleLengthRule(32),
            RouteOffsetRequestStopCountRangeRule(120, 86400),
            RouteOffsetRequestChannelLengthRule(16),
            RouteOffsetRequestRetriesRangeRule(80, 86400),
        ),
    )
}
