package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.kit.text.normalize

/** A rule a tiered route offset milestone must pass. Check returns its problems, or none. */
interface TieredRouteOffsetMilestoneRule {
    fun check(item: TieredRouteOffsetMilestone): MutableList<String>
}

data class TieredRouteOffsetMilestoneRequiredCodeRule(var message: String) : TieredRouteOffsetMilestoneRule {
    override fun check(item: TieredRouteOffsetMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRouteOffsetMilestoneCodeLengthRule(var maxLength: Int) : TieredRouteOffsetMilestoneRule {
    override fun check(item: TieredRouteOffsetMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRouteOffsetMilestoneTitleLengthRule(var maxLength: Int) : TieredRouteOffsetMilestoneRule {
    override fun check(item: TieredRouteOffsetMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRouteOffsetMilestoneAllowedTitleRule(
    var allowed: MutableList<String>,
) : TieredRouteOffsetMilestoneRule {
    override fun check(item: TieredRouteOffsetMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TieredRouteOffsetMilestoneDiscountRangeRule(
    var min: Double,
    var max: Double,
) : TieredRouteOffsetMilestoneRule {
    override fun check(item: TieredRouteOffsetMilestone): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class TieredRouteOffsetMilestoneRequiredTitleRule(var message: String) : TieredRouteOffsetMilestoneRule {
    override fun check(item: TieredRouteOffsetMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered route offset milestone. */
data class TieredRouteOffsetMilestoneValidator(var rules: MutableList<TieredRouteOffsetMilestoneRule>) {
    fun validate(item: TieredRouteOffsetMilestone): MutableList<String> {
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

    fun requireValid(item: TieredRouteOffsetMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRouteOffsetMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRouteOffsetMilestoneValidator(): TieredRouteOffsetMilestoneValidator {
    return TieredRouteOffsetMilestoneValidator(
        mutableListOf<TieredRouteOffsetMilestoneRule>(
            TieredRouteOffsetMilestoneRequiredCodeRule("tiered route offset milestone needs a value"),
            TieredRouteOffsetMilestoneCodeLengthRule(1000),
            TieredRouteOffsetMilestoneTitleLengthRule(64),
            TieredRouteOffsetMilestoneAllowedTitleRule(mutableListOf<String>()),
            TieredRouteOffsetMilestoneDiscountRangeRule(0.7, 2698.4),
            TieredRouteOffsetMilestoneRequiredTitleRule("tiered route offset milestone needs this field"),
        ),
    )
}
