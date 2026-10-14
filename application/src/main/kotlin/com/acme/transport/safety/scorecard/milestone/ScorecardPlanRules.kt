package com.acme.transport.safety.scorecard.milestone

/** A rule a scorecard plan must pass. Check returns its problems, or none. */
interface ScorecardPlanRule {
    fun check(item: ScorecardPlan): MutableList<String>
}

data class ScorecardPlanLabelLengthRule(var maxLength: Int) : ScorecardPlanRule {
    override fun check(item: ScorecardPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardPlanCodeLengthRule(var maxLength: Int) : ScorecardPlanRule {
    override fun check(item: ScorecardPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardPlanTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : ScorecardPlanRule {
    override fun check(item: ScorecardPlan): MutableList<String> {
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

data class ScorecardPlanMaximumRangeRule(var min: Double, var max: Double) : ScorecardPlanRule {
    override fun check(item: ScorecardPlan): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardPlanRequiredCodeRule(var message: String) : ScorecardPlanRule {
    override fun check(item: ScorecardPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard plan. */
data class ScorecardPlanValidator(var rules: MutableList<ScorecardPlanRule>) {
    fun validate(item: ScorecardPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardPlanValidator(): ScorecardPlanValidator {
    return ScorecardPlanValidator(
        mutableListOf<ScorecardPlanRule>(
            ScorecardPlanLabelLengthRule(86400),
            ScorecardPlanCodeLengthRule(3600),
            ScorecardPlanTagsLimitRule(1000, mutableListOf<String>("adaptive")),
            ScorecardPlanMaximumRangeRule(2.1, 88.8),
            ScorecardPlanRequiredCodeRule("scorecard plan needs a value"),
        ),
    )
}
