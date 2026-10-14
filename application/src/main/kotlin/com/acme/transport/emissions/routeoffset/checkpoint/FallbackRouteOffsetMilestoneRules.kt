package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a fallback route offset milestone must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetMilestoneRule {
    fun check(item: FallbackRouteOffsetMilestone): MutableList<String>
}

data class FallbackRouteOffsetMilestoneRequiredDepotRule(
    var message: String,
) : FallbackRouteOffsetMilestoneRule {
    override fun check(item: FallbackRouteOffsetMilestone): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetMilestoneAllowedDepotRule(
    var allowed: MutableList<String>,
) : FallbackRouteOffsetMilestoneRule {
    override fun check(item: FallbackRouteOffsetMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetMilestoneRetriesRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRouteOffsetMilestoneRule {
    override fun check(item: FallbackRouteOffsetMilestone): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetMilestonePalletsRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRouteOffsetMilestoneRule {
    override fun check(item: FallbackRouteOffsetMilestone): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset milestone. */
data class FallbackRouteOffsetMilestoneValidator(var rules: MutableList<FallbackRouteOffsetMilestoneRule>) {
    fun validate(item: FallbackRouteOffsetMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRouteOffsetMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetMilestoneValidator(): FallbackRouteOffsetMilestoneValidator {
    return FallbackRouteOffsetMilestoneValidator(
        mutableListOf<FallbackRouteOffsetMilestoneRule>(
            FallbackRouteOffsetMilestoneRequiredDepotRule("fallback route offset milestone needs an entry"),
            FallbackRouteOffsetMilestoneAllowedDepotRule(mutableListOf<String>("pending", "lenient")),
            FallbackRouteOffsetMilestoneRetriesRangeRule(120, 86400),
            FallbackRouteOffsetMilestonePalletsRangeRule(32, 255),
        ),
    )
}
