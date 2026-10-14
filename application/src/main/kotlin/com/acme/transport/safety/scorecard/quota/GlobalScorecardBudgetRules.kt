package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.text.normalize

/** A rule a global scorecard budget must pass. Check returns its problems, or none. */
interface GlobalScorecardBudgetRule {
    fun check(item: GlobalScorecardBudget): MutableList<String>
}

data class GlobalScorecardBudgetCodeLengthRule(var maxLength: Int) : GlobalScorecardBudgetRule {
    override fun check(item: GlobalScorecardBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalScorecardBudgetAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : GlobalScorecardBudgetRule {
    override fun check(item: GlobalScorecardBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalScorecardBudgetRequiredCodeRule(var message: String) : GlobalScorecardBudgetRule {
    override fun check(item: GlobalScorecardBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global scorecard budget. */
data class GlobalScorecardBudgetValidator(var rules: MutableList<GlobalScorecardBudgetRule>) {
    fun validate(item: GlobalScorecardBudget): MutableList<String> {
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

    fun requireValid(item: GlobalScorecardBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalScorecardBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalScorecardBudgetValidator(): GlobalScorecardBudgetValidator {
    return GlobalScorecardBudgetValidator(
        mutableListOf<GlobalScorecardBudgetRule>(
            GlobalScorecardBudgetCodeLengthRule(1000),
            GlobalScorecardBudgetAllowedTrailerIdRule(mutableListOf<String>()),
            GlobalScorecardBudgetRequiredCodeRule("global scorecard budget needs this field"),
        ),
    )
}
