package com.acme.transport.crewing.roster.budget

/** A rule a default roster budget must pass. Check returns its problems, or none. */
interface DefaultRosterBudgetRule {
    fun check(item: DefaultRosterBudget): MutableList<String>
}

data class DefaultRosterBudgetRevisionRangeRule(var min: Int, var max: Int) : DefaultRosterBudgetRule {
    override fun check(item: DefaultRosterBudget): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterBudgetCodePrefixRule(var prefix: String) : DefaultRosterBudgetRule {
    override fun check(item: DefaultRosterBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterBudgetSegmentLengthRule(var maxLength: Int) : DefaultRosterBudgetRule {
    override fun check(item: DefaultRosterBudget): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default roster budget. */
data class DefaultRosterBudgetValidator(var rules: MutableList<DefaultRosterBudgetRule>) {
    fun validate(item: DefaultRosterBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRosterBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRosterBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRosterBudgetValidator(): DefaultRosterBudgetValidator {
    return DefaultRosterBudgetValidator(
        mutableListOf<DefaultRosterBudgetRule>(
            DefaultRosterBudgetRevisionRangeRule(16, 1000),
            DefaultRosterBudgetCodePrefixRule("Q"),
            DefaultRosterBudgetSegmentLengthRule(64),
        ),
    )
}
