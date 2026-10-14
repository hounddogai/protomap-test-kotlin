package com.acme.transport.crewing.restbreak.entry

/** A rule a rest break budget must pass. Check returns its problems, or none. */
interface RestBreakBudgetRule {
    fun check(item: RestBreakBudget): MutableList<String>
}

data class RestBreakBudgetAmountRangeRule(var min: Double, var max: Double) : RestBreakBudgetRule {
    override fun check(item: RestBreakBudget): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class RestBreakBudgetQuantityRangeRule(var min: Int, var max: Int) : RestBreakBudgetRule {
    override fun check(item: RestBreakBudget): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakBudgetCodePrefixRule(var prefix: String) : RestBreakBudgetRule {
    override fun check(item: RestBreakBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakBudgetCodeLengthRule(var maxLength: Int) : RestBreakBudgetRule {
    override fun check(item: RestBreakBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break budget. */
data class RestBreakBudgetValidator(var rules: MutableList<RestBreakBudgetRule>) {
    fun validate(item: RestBreakBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakBudgetValidator(): RestBreakBudgetValidator {
    return RestBreakBudgetValidator(
        mutableListOf<RestBreakBudgetRule>(
            RestBreakBudgetAmountRangeRule(8.1, 2427.3),
            RestBreakBudgetQuantityRangeRule(80, 80),
            RestBreakBudgetCodePrefixRule("REF"),
            RestBreakBudgetCodeLengthRule(32),
        ),
    )
}
