package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.text.normalize

/** A rule a rolling route offset plan must pass. Check returns its problems, or none. */
interface RollingRouteOffsetPlanRule {
    fun check(item: RollingRouteOffsetPlan): MutableList<String>
}

data class RollingRouteOffsetPlanLabelLengthRule(var maxLength: Int) : RollingRouteOffsetPlanRule {
    override fun check(item: RollingRouteOffsetPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRouteOffsetPlanAllowedDockRule(
    var allowed: MutableList<String>,
) : RollingRouteOffsetPlanRule {
    override fun check(item: RollingRouteOffsetPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RollingRouteOffsetPlanRouteCodeLengthRule(var maxLength: Int) : RollingRouteOffsetPlanRule {
    override fun check(item: RollingRouteOffsetPlan): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling route offset plan. */
data class RollingRouteOffsetPlanValidator(var rules: MutableList<RollingRouteOffsetPlanRule>) {
    fun validate(item: RollingRouteOffsetPlan): MutableList<String> {
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

    fun requireValid(item: RollingRouteOffsetPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRouteOffsetPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRouteOffsetPlanValidator(): RollingRouteOffsetPlanValidator {
    return RollingRouteOffsetPlanValidator(
        mutableListOf<RollingRouteOffsetPlanRule>(
            RollingRouteOffsetPlanLabelLengthRule(3600),
            RollingRouteOffsetPlanAllowedDockRule(mutableListOf<String>("batched", "tiered")),
            RollingRouteOffsetPlanRouteCodeLengthRule(32),
        ),
    )
}
