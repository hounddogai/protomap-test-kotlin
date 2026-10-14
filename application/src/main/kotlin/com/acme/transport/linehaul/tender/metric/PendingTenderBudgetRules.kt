package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.text.normalize

/** A rule a pending tender budget must pass. Check returns its problems, or none. */
interface PendingTenderBudgetRule {
    fun check(item: PendingTenderBudget): MutableList<String>
}

data class PendingTenderBudgetRequiredTitleRule(var message: String) : PendingTenderBudgetRule {
    override fun check(item: PendingTenderBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderBudgetAllowedTitleRule(var allowed: MutableList<String>) : PendingTenderBudgetRule {
    override fun check(item: PendingTenderBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderBudgetRequiredCodeRule(var message: String) : PendingTenderBudgetRule {
    override fun check(item: PendingTenderBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tender budget. */
data class PendingTenderBudgetValidator(var rules: MutableList<PendingTenderBudgetRule>) {
    fun validate(item: PendingTenderBudget): MutableList<String> {
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

    fun requireValid(item: PendingTenderBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTenderBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTenderBudgetValidator(): PendingTenderBudgetValidator {
    return PendingTenderBudgetValidator(
        mutableListOf<PendingTenderBudgetRule>(
            PendingTenderBudgetRequiredTitleRule("pending tender budget needs this field"),
            PendingTenderBudgetAllowedTitleRule(mutableListOf<String>()),
            PendingTenderBudgetRequiredCodeRule("pending tender budget needs a value"),
        ),
    )
}
