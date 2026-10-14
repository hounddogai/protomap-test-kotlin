package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.text.normalize

/** A rule a global rest break budget must pass. Check returns its problems, or none. */
interface GlobalRestBreakBudgetRule {
    fun check(item: GlobalRestBreakBudget): MutableList<String>
}

data class GlobalRestBreakBudgetStopCountRangeRule(var min: Int, var max: Int) : GlobalRestBreakBudgetRule {
    override fun check(item: GlobalRestBreakBudget): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class GlobalRestBreakBudgetAllowedLabelRule(var allowed: MutableList<String>) : GlobalRestBreakBudgetRule {
    override fun check(item: GlobalRestBreakBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakBudgetUpdatedAtRangeRule(var min: Int, var max: Int) : GlobalRestBreakBudgetRule {
    override fun check(item: GlobalRestBreakBudget): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class GlobalRestBreakBudgetLabelLengthRule(var maxLength: Int) : GlobalRestBreakBudgetRule {
    override fun check(item: GlobalRestBreakBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakBudgetDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : GlobalRestBreakBudgetRule {
    override fun check(item: GlobalRestBreakBudget): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakBudgetDescriptionLengthRule(var maxLength: Int) : GlobalRestBreakBudgetRule {
    override fun check(item: GlobalRestBreakBudget): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global rest break budget. */
data class GlobalRestBreakBudgetValidator(var rules: MutableList<GlobalRestBreakBudgetRule>) {
    fun validate(item: GlobalRestBreakBudget): MutableList<String> {
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

    fun requireValid(item: GlobalRestBreakBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRestBreakBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRestBreakBudgetValidator(): GlobalRestBreakBudgetValidator {
    return GlobalRestBreakBudgetValidator(
        mutableListOf<GlobalRestBreakBudgetRule>(
            GlobalRestBreakBudgetStopCountRangeRule(80, 1000),
            GlobalRestBreakBudgetAllowedLabelRule(mutableListOf<String>("adaptive", "strict")),
            GlobalRestBreakBudgetUpdatedAtRangeRule(120, 3600),
            GlobalRestBreakBudgetLabelLengthRule(86400),
            GlobalRestBreakBudgetDistanceKmRangeRule(6.5, 3737.1),
            GlobalRestBreakBudgetDescriptionLengthRule(64),
        ),
    )
}
