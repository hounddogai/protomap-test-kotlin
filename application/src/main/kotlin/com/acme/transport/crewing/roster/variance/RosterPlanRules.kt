package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.text.normalize

/** A rule a roster plan must pass. Check returns its problems, or none. */
interface RosterPlanRule {
    fun check(item: RosterPlan): MutableList<String>
}

data class RosterPlanRequiredTitleRule(var message: String) : RosterPlanRule {
    override fun check(item: RosterPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterPlanLabelLengthRule(var maxLength: Int) : RosterPlanRule {
    override fun check(item: RosterPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterPlanPriorityRangeRule(var min: Int, var max: Int) : RosterPlanRule {
    override fun check(item: RosterPlan): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterPlanAllowedLabelRule(var allowed: MutableList<String>) : RosterPlanRule {
    override fun check(item: RosterPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RosterPlanRequiredTimezoneRule(var message: String) : RosterPlanRule {
    override fun check(item: RosterPlan): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster plan. */
data class RosterPlanValidator(var rules: MutableList<RosterPlanRule>) {
    fun validate(item: RosterPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterPlanValidator(): RosterPlanValidator {
    return RosterPlanValidator(
        mutableListOf<RosterPlanRule>(
            RosterPlanRequiredTitleRule("roster plan needs a value"),
            RosterPlanLabelLengthRule(3600),
            RosterPlanPriorityRangeRule(64, 80),
            RosterPlanAllowedLabelRule(mutableListOf<String>("weighted")),
            RosterPlanRequiredTimezoneRule("roster plan needs a value"),
        ),
    )
}
