package com.acme.transport.crewing.restbreak.metric

/** A rule a strict rest break plan must pass. Check returns its problems, or none. */
interface StrictRestBreakPlanRule {
    fun check(item: StrictRestBreakPlan): MutableList<String>
}

data class StrictRestBreakPlanArchivedGuardRule(var limit: Int) : StrictRestBreakPlanRule {
    override fun check(item: StrictRestBreakPlan): MutableList<String> {
        if (item.archived && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakPlanTitleLengthRule(var maxLength: Int) : StrictRestBreakPlanRule {
    override fun check(item: StrictRestBreakPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakPlanRequiredTitleRule(var message: String) : StrictRestBreakPlanRule {
    override fun check(item: StrictRestBreakPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakPlanCodeLengthRule(var maxLength: Int) : StrictRestBreakPlanRule {
    override fun check(item: StrictRestBreakPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakPlanCategoryLengthRule(var maxLength: Int) : StrictRestBreakPlanRule {
    override fun check(item: StrictRestBreakPlan): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakPlanRatioRangeRule(var min: Double, var max: Double) : StrictRestBreakPlanRule {
    override fun check(item: StrictRestBreakPlan): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rest break plan. */
data class StrictRestBreakPlanValidator(var rules: MutableList<StrictRestBreakPlanRule>) {
    fun validate(item: StrictRestBreakPlan): MutableList<String> {
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

    fun requireValid(item: StrictRestBreakPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRestBreakPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRestBreakPlanValidator(): StrictRestBreakPlanValidator {
    return StrictRestBreakPlanValidator(
        mutableListOf<StrictRestBreakPlanRule>(
            StrictRestBreakPlanArchivedGuardRule(120),
            StrictRestBreakPlanTitleLengthRule(64),
            StrictRestBreakPlanRequiredTitleRule("strict rest break plan needs a value"),
            StrictRestBreakPlanCodeLengthRule(64),
            StrictRestBreakPlanCategoryLengthRule(32),
            StrictRestBreakPlanRatioRangeRule(2.6, 2051.9),
        ),
    )
}
