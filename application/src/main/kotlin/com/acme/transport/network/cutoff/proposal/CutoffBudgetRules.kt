package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.normalize

/** A rule a cutoff budget must pass. Check returns its problems, or none. */
interface CutoffBudgetRule {
    fun check(item: CutoffBudget): MutableList<String>
}

data class CutoffBudgetRequiredNameRule(var message: String) : CutoffBudgetRule {
    override fun check(item: CutoffBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffBudgetAllowedCodeRule(var allowed: MutableList<String>) : CutoffBudgetRule {
    override fun check(item: CutoffBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CutoffBudgetPriorityRangeRule(var min: Int, var max: Int) : CutoffBudgetRule {
    override fun check(item: CutoffBudget): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class CutoffBudgetCodePrefixRule(var prefix: String) : CutoffBudgetRule {
    override fun check(item: CutoffBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff budget. */
data class CutoffBudgetValidator(var rules: MutableList<CutoffBudgetRule>) {
    fun validate(item: CutoffBudget): MutableList<String> {
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

    fun requireValid(item: CutoffBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffBudgetValidator(): CutoffBudgetValidator {
    return CutoffBudgetValidator(
        mutableListOf<CutoffBudgetRule>(
            CutoffBudgetRequiredNameRule("cutoff budget needs an entry"),
            CutoffBudgetAllowedCodeRule(mutableListOf<String>()),
            CutoffBudgetPriorityRangeRule(32, 255),
            CutoffBudgetCodePrefixRule("X"),
        ),
    )
}
