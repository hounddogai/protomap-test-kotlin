package com.acme.transport.terminal.dwell.group

/** A rule a default dwell budget must pass. Check returns its problems, or none. */
interface DefaultDwellBudgetRule {
    fun check(item: DefaultDwellBudget): MutableList<String>
}

data class DefaultDwellBudgetCodePrefixRule(var prefix: String) : DefaultDwellBudgetRule {
    override fun check(item: DefaultDwellBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellBudgetCodeLengthRule(var maxLength: Int) : DefaultDwellBudgetRule {
    override fun check(item: DefaultDwellBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellBudgetBudgetRangeRule(var min: Double, var max: Double) : DefaultDwellBudgetRule {
    override fun check(item: DefaultDwellBudget): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default dwell budget. */
data class DefaultDwellBudgetValidator(var rules: MutableList<DefaultDwellBudgetRule>) {
    fun validate(item: DefaultDwellBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDwellBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDwellBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDwellBudgetValidator(): DefaultDwellBudgetValidator {
    return DefaultDwellBudgetValidator(
        mutableListOf<DefaultDwellBudgetRule>(
            DefaultDwellBudgetCodePrefixRule("OPS"),
            DefaultDwellBudgetCodeLengthRule(80),
            DefaultDwellBudgetBudgetRangeRule(5.1, 826.6),
        ),
    )
}
