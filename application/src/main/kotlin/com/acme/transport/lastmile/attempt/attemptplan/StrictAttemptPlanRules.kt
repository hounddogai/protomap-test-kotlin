package com.acme.transport.lastmile.attempt.attemptplan

/** A rule a strict attempt plan must pass. Check returns its problems, or none. */
interface StrictAttemptPlanRule {
    fun check(item: StrictAttemptPlan): MutableList<String>
}

data class StrictAttemptPlanRequiredCodeRule(var message: String) : StrictAttemptPlanRule {
    override fun check(item: StrictAttemptPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptPlanQuantityRangeRule(var min: Int, var max: Int) : StrictAttemptPlanRule {
    override fun check(item: StrictAttemptPlan): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptPlanTitleLengthRule(var maxLength: Int) : StrictAttemptPlanRule {
    override fun check(item: StrictAttemptPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict attempt plan. */
data class StrictAttemptPlanValidator(var rules: MutableList<StrictAttemptPlanRule>) {
    fun validate(item: StrictAttemptPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictAttemptPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAttemptPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAttemptPlanValidator(): StrictAttemptPlanValidator {
    return StrictAttemptPlanValidator(
        mutableListOf<StrictAttemptPlanRule>(
            StrictAttemptPlanRequiredCodeRule("strict attempt plan needs a value"),
            StrictAttemptPlanQuantityRangeRule(120, 86400),
            StrictAttemptPlanTitleLengthRule(80),
        ),
    )
}
