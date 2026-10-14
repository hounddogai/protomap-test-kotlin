package com.acme.transport.safety.nearmiss.milestone

/** A rule a legacy near miss budget must pass. Check returns its problems, or none. */
interface LegacyNearMissBudgetRule {
    fun check(item: LegacyNearMissBudget): MutableList<String>
}

data class LegacyNearMissBudgetRequiredLabelRule(var message: String) : LegacyNearMissBudgetRule {
    override fun check(item: LegacyNearMissBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyNearMissBudgetApprovedGuardRule(var limit: Int) : LegacyNearMissBudgetRule {
    override fun check(item: LegacyNearMissBudget): MutableList<String> {
        if (item.approved && item.graceDays > limit) {
            return mutableListOf<String>("grace days ${item.graceDays} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class LegacyNearMissBudgetLabelLengthRule(var maxLength: Int) : LegacyNearMissBudgetRule {
    override fun check(item: LegacyNearMissBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy near miss budget. */
data class LegacyNearMissBudgetValidator(var rules: MutableList<LegacyNearMissBudgetRule>) {
    fun validate(item: LegacyNearMissBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyNearMissBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyNearMissBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyNearMissBudgetValidator(): LegacyNearMissBudgetValidator {
    return LegacyNearMissBudgetValidator(
        mutableListOf<LegacyNearMissBudgetRule>(
            LegacyNearMissBudgetRequiredLabelRule("legacy near miss budget needs this field"),
            LegacyNearMissBudgetApprovedGuardRule(120),
            LegacyNearMissBudgetLabelLengthRule(16),
        ),
    )
}
