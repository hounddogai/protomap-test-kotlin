package com.acme.transport.safety.violation.metric

/** A rule a rolling violation budget must pass. Check returns its problems, or none. */
interface RollingViolationBudgetRule {
    fun check(item: RollingViolationBudget): MutableList<String>
}

data class RollingViolationBudgetTitleLengthRule(var maxLength: Int) : RollingViolationBudgetRule {
    override fun check(item: RollingViolationBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationBudgetRequiredTitleRule(var message: String) : RollingViolationBudgetRule {
    override fun check(item: RollingViolationBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationBudgetRequiredTrailerIdRule(var message: String) : RollingViolationBudgetRule {
    override fun check(item: RollingViolationBudget): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling violation budget. */
data class RollingViolationBudgetValidator(var rules: MutableList<RollingViolationBudgetRule>) {
    fun validate(item: RollingViolationBudget): MutableList<String> {
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

    fun requireValid(item: RollingViolationBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingViolationBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingViolationBudgetValidator(): RollingViolationBudgetValidator {
    return RollingViolationBudgetValidator(
        mutableListOf<RollingViolationBudgetRule>(
            RollingViolationBudgetTitleLengthRule(64),
            RollingViolationBudgetRequiredTitleRule("rolling violation budget needs a value"),
            RollingViolationBudgetRequiredTrailerIdRule("rolling violation budget needs an entry"),
        ),
    )
}
