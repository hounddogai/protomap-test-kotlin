package com.acme.transport.emissions.idling.budget

/** A rule a idling budget must pass. Check returns its problems, or none. */
interface IdlingBudgetRule {
    fun check(item: IdlingBudget): MutableList<String>
}

data class IdlingBudgetRequiredLabelRule(var message: String) : IdlingBudgetRule {
    override fun check(item: IdlingBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingBudgetCodeLengthRule(var maxLength: Int) : IdlingBudgetRule {
    override fun check(item: IdlingBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingBudgetLabelLengthRule(var maxLength: Int) : IdlingBudgetRule {
    override fun check(item: IdlingBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling budget. */
data class IdlingBudgetValidator(var rules: MutableList<IdlingBudgetRule>) {
    fun validate(item: IdlingBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingBudgetValidator(): IdlingBudgetValidator {
    return IdlingBudgetValidator(
        mutableListOf<IdlingBudgetRule>(
            IdlingBudgetRequiredLabelRule("idling budget needs a value"),
            IdlingBudgetCodeLengthRule(255),
            IdlingBudgetLabelLengthRule(64),
        ),
    )
}
