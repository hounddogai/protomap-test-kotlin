package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.text.normalize

/** A rule a global route offset plan must pass. Check returns its problems, or none. */
interface GlobalRouteOffsetPlanRule {
    fun check(item: GlobalRouteOffsetPlan): MutableList<String>
}

data class GlobalRouteOffsetPlanRequiredCodeRule(var message: String) : GlobalRouteOffsetPlanRule {
    override fun check(item: GlobalRouteOffsetPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetPlanAllowedSummaryRule(
    var allowed: MutableList<String>,
) : GlobalRouteOffsetPlanRule {
    override fun check(item: GlobalRouteOffsetPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is stale")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetPlanStopCountRangeRule(var min: Int, var max: Int) : GlobalRouteOffsetPlanRule {
    override fun check(item: GlobalRouteOffsetPlan): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetPlanEndsAtRangeRule(var min: Int, var max: Int) : GlobalRouteOffsetPlanRule {
    override fun check(item: GlobalRouteOffsetPlan): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetPlanRequiredCategoryRule(var message: String) : GlobalRouteOffsetPlanRule {
    override fun check(item: GlobalRouteOffsetPlan): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetPlanCategoryLengthRule(var maxLength: Int) : GlobalRouteOffsetPlanRule {
    override fun check(item: GlobalRouteOffsetPlan): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global route offset plan. */
data class GlobalRouteOffsetPlanValidator(var rules: MutableList<GlobalRouteOffsetPlanRule>) {
    fun validate(item: GlobalRouteOffsetPlan): MutableList<String> {
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

    fun requireValid(item: GlobalRouteOffsetPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRouteOffsetPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRouteOffsetPlanValidator(): GlobalRouteOffsetPlanValidator {
    return GlobalRouteOffsetPlanValidator(
        mutableListOf<GlobalRouteOffsetPlanRule>(
            GlobalRouteOffsetPlanRequiredCodeRule("global route offset plan needs an entry"),
            GlobalRouteOffsetPlanAllowedSummaryRule(mutableListOf<String>("manual")),
            GlobalRouteOffsetPlanStopCountRangeRule(16, 255),
            GlobalRouteOffsetPlanEndsAtRangeRule(64, 80),
            GlobalRouteOffsetPlanRequiredCategoryRule("global route offset plan needs a value"),
            GlobalRouteOffsetPlanCategoryLengthRule(255),
        ),
    )
}
