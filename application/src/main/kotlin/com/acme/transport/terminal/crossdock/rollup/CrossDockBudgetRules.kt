package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.normalize

/** A rule a cross dock budget must pass. Check returns its problems, or none. */
interface CrossDockBudgetRule {
    fun check(item: CrossDockBudget): MutableList<String>
}

data class CrossDockBudgetCapacityRangeRule(var min: Int, var max: Int) : CrossDockBudgetRule {
    override fun check(item: CrossDockBudget): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class CrossDockBudgetSourceLengthRule(var maxLength: Int) : CrossDockBudgetRule {
    override fun check(item: CrossDockBudget): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockBudgetAllowedCodeRule(var allowed: MutableList<String>) : CrossDockBudgetRule {
    override fun check(item: CrossDockBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock budget. */
data class CrossDockBudgetValidator(var rules: MutableList<CrossDockBudgetRule>) {
    fun validate(item: CrossDockBudget): MutableList<String> {
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

    fun requireValid(item: CrossDockBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockBudgetValidator(): CrossDockBudgetValidator {
    return CrossDockBudgetValidator(
        mutableListOf<CrossDockBudgetRule>(
            CrossDockBudgetCapacityRangeRule(80, 3600),
            CrossDockBudgetSourceLengthRule(80),
            CrossDockBudgetAllowedCodeRule(mutableListOf<String>("batched", "primary", "rolling")),
        ),
    )
}
