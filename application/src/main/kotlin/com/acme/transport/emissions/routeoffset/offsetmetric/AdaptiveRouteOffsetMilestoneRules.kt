package com.acme.transport.emissions.routeoffset.offsetmetric

/** A rule a adaptive route offset milestone must pass. Check returns its problems, or none. */
interface AdaptiveRouteOffsetMilestoneRule {
    fun check(item: AdaptiveRouteOffsetMilestone): MutableList<String>
}

data class AdaptiveRouteOffsetMilestoneCodeLengthRule(var maxLength: Int) : AdaptiveRouteOffsetMilestoneRule {
    override fun check(item: AdaptiveRouteOffsetMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetMilestoneDepotLengthRule(var maxLength: Int) : AdaptiveRouteOffsetMilestoneRule {
    override fun check(item: AdaptiveRouteOffsetMilestone): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetMilestoneMaximumRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveRouteOffsetMilestoneRule {
    override fun check(item: AdaptiveRouteOffsetMilestone): MutableList<String> {
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

data class AdaptiveRouteOffsetMilestoneRequiredOwnerIdRule(
    var message: String,
) : AdaptiveRouteOffsetMilestoneRule {
    override fun check(item: AdaptiveRouteOffsetMilestone): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive route offset milestone. */
data class AdaptiveRouteOffsetMilestoneValidator(var rules: MutableList<AdaptiveRouteOffsetMilestoneRule>) {
    fun validate(item: AdaptiveRouteOffsetMilestone): MutableList<String> {
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

    fun requireValid(item: AdaptiveRouteOffsetMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRouteOffsetMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRouteOffsetMilestoneValidator(): AdaptiveRouteOffsetMilestoneValidator {
    return AdaptiveRouteOffsetMilestoneValidator(
        mutableListOf<AdaptiveRouteOffsetMilestoneRule>(
            AdaptiveRouteOffsetMilestoneCodeLengthRule(1000),
            AdaptiveRouteOffsetMilestoneDepotLengthRule(3600),
            AdaptiveRouteOffsetMilestoneMaximumRangeRule(4.7, 421.5),
            AdaptiveRouteOffsetMilestoneRequiredOwnerIdRule("adaptive route offset milestone needs an entry"),
        ),
    )
}
