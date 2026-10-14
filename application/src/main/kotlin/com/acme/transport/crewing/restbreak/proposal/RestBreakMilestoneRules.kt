package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.text.normalize

/** A rule a rest break milestone must pass. Check returns its problems, or none. */
interface RestBreakMilestoneRule {
    fun check(item: RestBreakMilestone): MutableList<String>
}

data class RestBreakMilestoneAllowedCodeRule(var allowed: MutableList<String>) : RestBreakMilestoneRule {
    override fun check(item: RestBreakMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakMilestonePinnedGuardRule(var limit: Int) : RestBreakMilestoneRule {
    override fun check(item: RestBreakMilestone): MutableList<String> {
        if (item.pinned && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakMilestoneRequiredLaneRule(var message: String) : RestBreakMilestoneRule {
    override fun check(item: RestBreakMilestone): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break milestone. */
data class RestBreakMilestoneValidator(var rules: MutableList<RestBreakMilestoneRule>) {
    fun validate(item: RestBreakMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakMilestoneValidator(): RestBreakMilestoneValidator {
    return RestBreakMilestoneValidator(
        mutableListOf<RestBreakMilestoneRule>(
            RestBreakMilestoneAllowedCodeRule(mutableListOf<String>()),
            RestBreakMilestonePinnedGuardRule(86400),
            RestBreakMilestoneRequiredLaneRule("rest break milestone needs an entry"),
        ),
    )
}
