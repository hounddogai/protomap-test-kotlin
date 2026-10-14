package com.acme.transport.safety.coaching.rollup

/** A rule a default coaching budget must pass. Check returns its problems, or none. */
interface DefaultCoachingBudgetRule {
    fun check(item: DefaultCoachingBudget): MutableList<String>
}

data class DefaultCoachingBudgetSourceLengthRule(var maxLength: Int) : DefaultCoachingBudgetRule {
    override fun check(item: DefaultCoachingBudget): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingBudgetRateRangeRule(var min: Double, var max: Double) : DefaultCoachingBudgetRule {
    override fun check(item: DefaultCoachingBudget): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingBudgetRequiredLabelRule(var message: String) : DefaultCoachingBudgetRule {
    override fun check(item: DefaultCoachingBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingBudgetCodePrefixRule(var prefix: String) : DefaultCoachingBudgetRule {
    override fun check(item: DefaultCoachingBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingBudgetLabelLengthRule(var maxLength: Int) : DefaultCoachingBudgetRule {
    override fun check(item: DefaultCoachingBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingBudgetRequiredSourceRule(var message: String) : DefaultCoachingBudgetRule {
    override fun check(item: DefaultCoachingBudget): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default coaching budget. */
data class DefaultCoachingBudgetValidator(var rules: MutableList<DefaultCoachingBudgetRule>) {
    fun validate(item: DefaultCoachingBudget): MutableList<String> {
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

    fun requireValid(item: DefaultCoachingBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCoachingBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCoachingBudgetValidator(): DefaultCoachingBudgetValidator {
    return DefaultCoachingBudgetValidator(
        mutableListOf<DefaultCoachingBudgetRule>(
            DefaultCoachingBudgetSourceLengthRule(86400),
            DefaultCoachingBudgetRateRangeRule(1.5, 353.1),
            DefaultCoachingBudgetRequiredLabelRule("default coaching budget needs an entry"),
            DefaultCoachingBudgetCodePrefixRule("Q"),
            DefaultCoachingBudgetLabelLengthRule(255),
            DefaultCoachingBudgetRequiredSourceRule("default coaching budget needs a value"),
        ),
    )
}
