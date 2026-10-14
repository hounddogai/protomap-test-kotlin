package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.text.normalize

/** A rule a rolling incident plan must pass. Check returns its problems, or none. */
interface RollingIncidentPlanRule {
    fun check(item: RollingIncidentPlan): MutableList<String>
}

data class RollingIncidentPlanAllowedCodeRule(var allowed: MutableList<String>) : RollingIncidentPlanRule {
    override fun check(item: RollingIncidentPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentPlanCodeLengthRule(var maxLength: Int) : RollingIncidentPlanRule {
    override fun check(item: RollingIncidentPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentPlanApprovedGuardRule(var limit: Double) : RollingIncidentPlanRule {
    override fun check(item: RollingIncidentPlan): MutableList<String> {
        if (item.approved && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentPlanTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingIncidentPlanRule {
    override fun check(item: RollingIncidentPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a rolling incident plan. */
data class RollingIncidentPlanValidator(var rules: MutableList<RollingIncidentPlanRule>) {
    fun validate(item: RollingIncidentPlan): MutableList<String> {
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

    fun requireValid(item: RollingIncidentPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingIncidentPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingIncidentPlanValidator(): RollingIncidentPlanValidator {
    return RollingIncidentPlanValidator(
        mutableListOf<RollingIncidentPlanRule>(
            RollingIncidentPlanAllowedCodeRule(mutableListOf<String>("tiered", "rolling", "lenient")),
            RollingIncidentPlanCodeLengthRule(86400),
            RollingIncidentPlanApprovedGuardRule(9.9),
            RollingIncidentPlanTagsLimitRule(120, mutableListOf<String>()),
        ),
    )
}
