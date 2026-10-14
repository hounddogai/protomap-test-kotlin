package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.kit.text.normalize

/** A rule a weighted idling budget must pass. Check returns its problems, or none. */
interface WeightedIdlingBudgetRule {
    fun check(item: WeightedIdlingBudget): MutableList<String>
}

data class WeightedIdlingBudgetCodeLengthRule(var maxLength: Int) : WeightedIdlingBudgetRule {
    override fun check(item: WeightedIdlingBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedIdlingBudgetAllowedCodeRule(var allowed: MutableList<String>) : WeightedIdlingBudgetRule {
    override fun check(item: WeightedIdlingBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedIdlingBudgetGraceDaysRangeRule(var min: Int, var max: Int) : WeightedIdlingBudgetRule {
    override fun check(item: WeightedIdlingBudget): MutableList<String> {
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

data class WeightedIdlingBudgetRequiredChannelRule(var message: String) : WeightedIdlingBudgetRule {
    override fun check(item: WeightedIdlingBudget): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted idling budget. */
data class WeightedIdlingBudgetValidator(var rules: MutableList<WeightedIdlingBudgetRule>) {
    fun validate(item: WeightedIdlingBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedIdlingBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedIdlingBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedIdlingBudgetValidator(): WeightedIdlingBudgetValidator {
    return WeightedIdlingBudgetValidator(
        mutableListOf<WeightedIdlingBudgetRule>(
            WeightedIdlingBudgetCodeLengthRule(32),
            WeightedIdlingBudgetAllowedCodeRule(mutableListOf<String>("global", "legacy")),
            WeightedIdlingBudgetGraceDaysRangeRule(80, 3600),
            WeightedIdlingBudgetRequiredChannelRule("weighted idling budget needs a value"),
        ),
    )
}
