package com.acme.transport.safety.violation.variance

import com.acme.transport.kit.text.normalize

/** A rule a global violation budget must pass. Check returns its problems, or none. */
interface GlobalViolationBudgetRule {
    fun check(item: GlobalViolationBudget): MutableList<String>
}

data class GlobalViolationBudgetRequiredNotesRule(var message: String) : GlobalViolationBudgetRule {
    override fun check(item: GlobalViolationBudget): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationBudgetStartsAtRangeRule(var min: Int, var max: Int) : GlobalViolationBudgetRule {
    override fun check(item: GlobalViolationBudget): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationBudgetAllowedRegionRule(
    var allowed: MutableList<String>,
) : GlobalViolationBudgetRule {
    override fun check(item: GlobalViolationBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationBudgetNotesLengthRule(var maxLength: Int) : GlobalViolationBudgetRule {
    override fun check(item: GlobalViolationBudget): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationBudgetGraceDaysRangeRule(var min: Int, var max: Int) : GlobalViolationBudgetRule {
    override fun check(item: GlobalViolationBudget): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class GlobalViolationBudgetCodeLengthRule(var maxLength: Int) : GlobalViolationBudgetRule {
    override fun check(item: GlobalViolationBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global violation budget. */
data class GlobalViolationBudgetValidator(var rules: MutableList<GlobalViolationBudgetRule>) {
    fun validate(item: GlobalViolationBudget): MutableList<String> {
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

    fun requireValid(item: GlobalViolationBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalViolationBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalViolationBudgetValidator(): GlobalViolationBudgetValidator {
    return GlobalViolationBudgetValidator(
        mutableListOf<GlobalViolationBudgetRule>(
            GlobalViolationBudgetRequiredNotesRule("global violation budget needs a value"),
            GlobalViolationBudgetStartsAtRangeRule(32, 86400),
            GlobalViolationBudgetAllowedRegionRule(mutableListOf<String>("regional")),
            GlobalViolationBudgetNotesLengthRule(86400),
            GlobalViolationBudgetGraceDaysRangeRule(64, 1000),
            GlobalViolationBudgetCodeLengthRule(80),
        ),
    )
}
