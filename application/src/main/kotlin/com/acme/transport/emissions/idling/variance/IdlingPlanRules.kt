package com.acme.transport.emissions.idling.variance

/** A rule a idling plan must pass. Check returns its problems, or none. */
interface IdlingPlanRule {
    fun check(item: IdlingPlan): MutableList<String>
}

data class IdlingPlanRequiredTitleRule(var message: String) : IdlingPlanRule {
    override fun check(item: IdlingPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingPlanThresholdRangeRule(var min: Double, var max: Double) : IdlingPlanRule {
    override fun check(item: IdlingPlan): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingPlanTitleLengthRule(var maxLength: Int) : IdlingPlanRule {
    override fun check(item: IdlingPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling plan. */
data class IdlingPlanValidator(var rules: MutableList<IdlingPlanRule>) {
    fun validate(item: IdlingPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingPlanValidator(): IdlingPlanValidator {
    return IdlingPlanValidator(
        mutableListOf<IdlingPlanRule>(
            IdlingPlanRequiredTitleRule("idling plan needs an entry"),
            IdlingPlanThresholdRangeRule(8.0, 3719.7),
            IdlingPlanTitleLengthRule(80),
        ),
    )
}
