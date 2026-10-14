package com.acme.transport.terminal.dwell.dwellmilestone

/** A rule a strict dwell plan must pass. Check returns its problems, or none. */
interface StrictDwellPlanRule {
    fun check(item: StrictDwellPlan): MutableList<String>
}

data class StrictDwellPlanCodeLengthRule(var maxLength: Int) : StrictDwellPlanRule {
    override fun check(item: StrictDwellPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellPlanVersionRangeRule(var min: Int, var max: Int) : StrictDwellPlanRule {
    override fun check(item: StrictDwellPlan): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class StrictDwellPlanRequiredCodeRule(var message: String) : StrictDwellPlanRule {
    override fun check(item: StrictDwellPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellPlanRequiredReviewerRule(var message: String) : StrictDwellPlanRule {
    override fun check(item: StrictDwellPlan): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellPlanReviewerLengthRule(var maxLength: Int) : StrictDwellPlanRule {
    override fun check(item: StrictDwellPlan): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellPlanMinimumRangeRule(var min: Double, var max: Double) : StrictDwellPlanRule {
    override fun check(item: StrictDwellPlan): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dwell plan. */
data class StrictDwellPlanValidator(var rules: MutableList<StrictDwellPlanRule>) {
    fun validate(item: StrictDwellPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictDwellPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDwellPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDwellPlanValidator(): StrictDwellPlanValidator {
    return StrictDwellPlanValidator(
        mutableListOf<StrictDwellPlanRule>(
            StrictDwellPlanCodeLengthRule(80),
            StrictDwellPlanVersionRangeRule(16, 1000),
            StrictDwellPlanRequiredCodeRule("strict dwell plan needs this field"),
            StrictDwellPlanRequiredReviewerRule("strict dwell plan needs a value"),
            StrictDwellPlanReviewerLengthRule(255),
            StrictDwellPlanMinimumRangeRule(9.9, 2180.7),
        ),
    )
}
