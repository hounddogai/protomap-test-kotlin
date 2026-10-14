package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.text.normalize

/** A rule a rolling roster plan must pass. Check returns its problems, or none. */
interface RollingRosterPlanRule {
    fun check(item: RollingRosterPlan): MutableList<String>
}

data class RollingRosterPlanLaneLengthRule(var maxLength: Int) : RollingRosterPlanRule {
    override fun check(item: RollingRosterPlan): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRosterPlanAllowedLaneRule(var allowed: MutableList<String>) : RollingRosterPlanRule {
    override fun check(item: RollingRosterPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RollingRosterPlanOwnerIdLengthRule(var maxLength: Int) : RollingRosterPlanRule {
    override fun check(item: RollingRosterPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRosterPlanCodeLengthRule(var maxLength: Int) : RollingRosterPlanRule {
    override fun check(item: RollingRosterPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling roster plan. */
data class RollingRosterPlanValidator(var rules: MutableList<RollingRosterPlanRule>) {
    fun validate(item: RollingRosterPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingRosterPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRosterPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRosterPlanValidator(): RollingRosterPlanValidator {
    return RollingRosterPlanValidator(
        mutableListOf<RollingRosterPlanRule>(
            RollingRosterPlanLaneLengthRule(255),
            RollingRosterPlanAllowedLaneRule(mutableListOf<String>("weighted", "primary", "adaptive")),
            RollingRosterPlanOwnerIdLengthRule(255),
            RollingRosterPlanCodeLengthRule(255),
        ),
    )
}
