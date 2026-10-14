package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.normalize

/** A rule a rolling locker plan must pass. Check returns its problems, or none. */
interface RollingLockerPlanRule {
    fun check(item: RollingLockerPlan): MutableList<String>
}

data class RollingLockerPlanAllowedSegmentRule(var allowed: MutableList<String>) : RollingLockerPlanRule {
    override fun check(item: RollingLockerPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RollingLockerPlanRequiredSegmentRule(var message: String) : RollingLockerPlanRule {
    override fun check(item: RollingLockerPlan): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingLockerPlanDurationMinutesRangeRule(var min: Int, var max: Int) : RollingLockerPlanRule {
    override fun check(item: RollingLockerPlan): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling locker plan. */
data class RollingLockerPlanValidator(var rules: MutableList<RollingLockerPlanRule>) {
    fun validate(item: RollingLockerPlan): MutableList<String> {
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

    fun requireValid(item: RollingLockerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingLockerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingLockerPlanValidator(): RollingLockerPlanValidator {
    return RollingLockerPlanValidator(
        mutableListOf<RollingLockerPlanRule>(
            RollingLockerPlanAllowedSegmentRule(mutableListOf<String>()),
            RollingLockerPlanRequiredSegmentRule("rolling locker plan needs this field"),
            RollingLockerPlanDurationMinutesRangeRule(16, 32),
        ),
    )
}
