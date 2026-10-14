package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.text.normalize

/** A rule a hub budget must pass. Check returns its problems, or none. */
interface HubBudgetRule {
    fun check(item: HubBudget): MutableList<String>
}

data class HubBudgetRateRangeRule(var min: Double, var max: Double) : HubBudgetRule {
    override fun check(item: HubBudget): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class HubBudgetMinimumRangeRule(var min: Double, var max: Double) : HubBudgetRule {
    override fun check(item: HubBudget): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class HubBudgetLimitRangeRule(var min: Int, var max: Int) : HubBudgetRule {
    override fun check(item: HubBudget): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class HubBudgetRequiredCodeRule(var message: String) : HubBudgetRule {
    override fun check(item: HubBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubBudgetTitleLengthRule(var maxLength: Int) : HubBudgetRule {
    override fun check(item: HubBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubBudgetAllowedTimezoneRule(var allowed: MutableList<String>) : HubBudgetRule {
    override fun check(item: HubBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub budget. */
data class HubBudgetValidator(var rules: MutableList<HubBudgetRule>) {
    fun validate(item: HubBudget): MutableList<String> {
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

    fun requireValid(item: HubBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultHubBudgetValidator(): HubBudgetValidator {
    return HubBudgetValidator(
        mutableListOf<HubBudgetRule>(
            HubBudgetRateRangeRule(6.4, 2759.7),
            HubBudgetMinimumRangeRule(9.0, 2777.4),
            HubBudgetLimitRangeRule(255, 3600),
            HubBudgetRequiredCodeRule("hub budget needs this field"),
            HubBudgetTitleLengthRule(255),
            HubBudgetAllowedTimezoneRule(mutableListOf<String>()),
        ),
    )
}
