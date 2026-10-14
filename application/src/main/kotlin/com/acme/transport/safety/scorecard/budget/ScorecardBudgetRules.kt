package com.acme.transport.safety.scorecard.budget

/** A rule a scorecard budget must pass. Check returns its problems, or none. */
interface ScorecardBudgetRule {
    fun check(item: ScorecardBudget): MutableList<String>
}

data class ScorecardBudgetRequiredSegmentRule(var message: String) : ScorecardBudgetRule {
    override fun check(item: ScorecardBudget): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardBudgetOwnerIdLengthRule(var maxLength: Int) : ScorecardBudgetRule {
    override fun check(item: ScorecardBudget): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardBudgetNameLengthRule(var maxLength: Int) : ScorecardBudgetRule {
    override fun check(item: ScorecardBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard budget. */
data class ScorecardBudgetValidator(var rules: MutableList<ScorecardBudgetRule>) {
    fun validate(item: ScorecardBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardBudgetValidator(): ScorecardBudgetValidator {
    return ScorecardBudgetValidator(
        mutableListOf<ScorecardBudgetRule>(
            ScorecardBudgetRequiredSegmentRule("scorecard budget needs a value"),
            ScorecardBudgetOwnerIdLengthRule(120),
            ScorecardBudgetNameLengthRule(80),
        ),
    )
}
