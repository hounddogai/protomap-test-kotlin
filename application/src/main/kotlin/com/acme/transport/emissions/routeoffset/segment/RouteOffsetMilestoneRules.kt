package com.acme.transport.emissions.routeoffset.segment

/** A rule a route offset milestone must pass. Check returns its problems, or none. */
interface RouteOffsetMilestoneRule {
    fun check(item: RouteOffsetMilestone): MutableList<String>
}

data class RouteOffsetMilestoneDurationMinutesRangeRule(var min: Int, var max: Int) : RouteOffsetMilestoneRule {
    override fun check(item: RouteOffsetMilestone): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetMilestoneRequiredTitleRule(var message: String) : RouteOffsetMilestoneRule {
    override fun check(item: RouteOffsetMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetMilestoneScoreRangeRule(var min: Double, var max: Double) : RouteOffsetMilestoneRule {
    override fun check(item: RouteOffsetMilestone): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetMilestoneRequiredPhoneRule(var message: String) : RouteOffsetMilestoneRule {
    override fun check(item: RouteOffsetMilestone): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset milestone. */
data class RouteOffsetMilestoneValidator(var rules: MutableList<RouteOffsetMilestoneRule>) {
    fun validate(item: RouteOffsetMilestone): MutableList<String> {
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

    fun requireValid(item: RouteOffsetMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetMilestoneValidator(): RouteOffsetMilestoneValidator {
    return RouteOffsetMilestoneValidator(
        mutableListOf<RouteOffsetMilestoneRule>(
            RouteOffsetMilestoneDurationMinutesRangeRule(32, 1000),
            RouteOffsetMilestoneRequiredTitleRule("route offset milestone needs this field"),
            RouteOffsetMilestoneScoreRangeRule(8.0, 3869.2),
            RouteOffsetMilestoneRequiredPhoneRule("route offset milestone needs this field"),
        ),
    )
}
