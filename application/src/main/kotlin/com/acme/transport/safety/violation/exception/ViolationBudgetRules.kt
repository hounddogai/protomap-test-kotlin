package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.normalize

/** A rule a violation budget must pass. Check returns its problems, or none. */
interface ViolationBudgetRule {
    fun check(item: ViolationBudget): MutableList<String>
}

data class ViolationBudgetNameLengthRule(var maxLength: Int) : ViolationBudgetRule {
    override fun check(item: ViolationBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationBudgetSummaryLengthRule(var maxLength: Int) : ViolationBudgetRule {
    override fun check(item: ViolationBudget): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationBudgetAllowedCodeRule(var allowed: MutableList<String>) : ViolationBudgetRule {
    override fun check(item: ViolationBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ViolationBudgetRequiredCodeRule(var message: String) : ViolationBudgetRule {
    override fun check(item: ViolationBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation budget. */
data class ViolationBudgetValidator(var rules: MutableList<ViolationBudgetRule>) {
    fun validate(item: ViolationBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationBudgetValidator(): ViolationBudgetValidator {
    return ViolationBudgetValidator(
        mutableListOf<ViolationBudgetRule>(
            ViolationBudgetNameLengthRule(32),
            ViolationBudgetSummaryLengthRule(32),
            ViolationBudgetAllowedCodeRule(mutableListOf<String>("primary", "legacy", "capped")),
            ViolationBudgetRequiredCodeRule("violation budget needs a value"),
        ),
    )
}
