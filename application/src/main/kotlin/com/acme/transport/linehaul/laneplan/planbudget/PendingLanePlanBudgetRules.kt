package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.text.normalize

/** A rule a pending lane plan budget must pass. Check returns its problems, or none. */
interface PendingLanePlanBudgetRule {
    fun check(item: PendingLanePlanBudget): MutableList<String>
}

data class PendingLanePlanBudgetWeightRangeRule(var min: Double, var max: Double) : PendingLanePlanBudgetRule {
    override fun check(item: PendingLanePlanBudget): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanBudgetAllowedTitleRule(var allowed: MutableList<String>) : PendingLanePlanBudgetRule {
    override fun check(item: PendingLanePlanBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanBudgetRequiredNameRule(var message: String) : PendingLanePlanBudgetRule {
    override fun check(item: PendingLanePlanBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending lane plan budget. */
data class PendingLanePlanBudgetValidator(var rules: MutableList<PendingLanePlanBudgetRule>) {
    fun validate(item: PendingLanePlanBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingLanePlanBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLanePlanBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLanePlanBudgetValidator(): PendingLanePlanBudgetValidator {
    return PendingLanePlanBudgetValidator(
        mutableListOf<PendingLanePlanBudgetRule>(
            PendingLanePlanBudgetWeightRangeRule(2.7, 3086.8),
            PendingLanePlanBudgetAllowedTitleRule(mutableListOf<String>("manual")),
            PendingLanePlanBudgetRequiredNameRule("pending lane plan budget needs this field"),
        ),
    )
}
