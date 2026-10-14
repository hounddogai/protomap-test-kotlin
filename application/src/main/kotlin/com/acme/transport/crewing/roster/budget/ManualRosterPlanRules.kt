package com.acme.transport.crewing.roster.budget

/** A rule a manual roster plan must pass. Check returns its problems, or none. */
interface ManualRosterPlanRule {
    fun check(item: ManualRosterPlan): MutableList<String>
}

data class ManualRosterPlanTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ManualRosterPlanRule {
    override fun check(item: ManualRosterPlan): MutableList<String> {
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

data class ManualRosterPlanRouteCodeLengthRule(var maxLength: Int) : ManualRosterPlanRule {
    override fun check(item: ManualRosterPlan): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRosterPlanCreatedAtRangeRule(var min: Int, var max: Int) : ManualRosterPlanRule {
    override fun check(item: ManualRosterPlan): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual roster plan. */
data class ManualRosterPlanValidator(var rules: MutableList<ManualRosterPlanRule>) {
    fun validate(item: ManualRosterPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRosterPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRosterPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRosterPlanValidator(): ManualRosterPlanValidator {
    return ManualRosterPlanValidator(
        mutableListOf<ManualRosterPlanRule>(
            ManualRosterPlanTagsLimitRule(1000, mutableListOf<String>("capped")),
            ManualRosterPlanRouteCodeLengthRule(86400),
            ManualRosterPlanCreatedAtRangeRule(80, 120),
        ),
    )
}
