package com.acme.transport.customs.clearance.segment

import com.acme.transport.kit.text.normalize

/** A rule a clearance milestone must pass. Check returns its problems, or none. */
interface ClearanceMilestoneRule {
    fun check(item: ClearanceMilestone): MutableList<String>
}

data class ClearanceMilestoneAllowedCodeRule(var allowed: MutableList<String>) : ClearanceMilestoneRule {
    override fun check(item: ClearanceMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceMilestoneRequiredLabelRule(var message: String) : ClearanceMilestoneRule {
    override fun check(item: ClearanceMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceMilestoneRequiredRouteCodeRule(var message: String) : ClearanceMilestoneRule {
    override fun check(item: ClearanceMilestone): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance milestone. */
data class ClearanceMilestoneValidator(var rules: MutableList<ClearanceMilestoneRule>) {
    fun validate(item: ClearanceMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceMilestoneValidator(): ClearanceMilestoneValidator {
    return ClearanceMilestoneValidator(
        mutableListOf<ClearanceMilestoneRule>(
            ClearanceMilestoneAllowedCodeRule(mutableListOf<String>("adaptive")),
            ClearanceMilestoneRequiredLabelRule("clearance milestone needs this field"),
            ClearanceMilestoneRequiredRouteCodeRule("clearance milestone needs a value"),
        ),
    )
}
