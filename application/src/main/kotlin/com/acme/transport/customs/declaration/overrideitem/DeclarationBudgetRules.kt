package com.acme.transport.customs.declaration.overrideitem

/** A rule a declaration budget must pass. Check returns its problems, or none. */
interface DeclarationBudgetRule {
    fun check(item: DeclarationBudget): MutableList<String>
}

data class DeclarationBudgetMaximumRangeRule(var min: Double, var max: Double) : DeclarationBudgetRule {
    override fun check(item: DeclarationBudget): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationBudgetRequiredCodeRule(var message: String) : DeclarationBudgetRule {
    override fun check(item: DeclarationBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationBudgetCodePrefixRule(var prefix: String) : DeclarationBudgetRule {
    override fun check(item: DeclarationBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration budget. */
data class DeclarationBudgetValidator(var rules: MutableList<DeclarationBudgetRule>) {
    fun validate(item: DeclarationBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationBudgetValidator(): DeclarationBudgetValidator {
    return DeclarationBudgetValidator(
        mutableListOf<DeclarationBudgetRule>(
            DeclarationBudgetMaximumRangeRule(3.3, 2994.9),
            DeclarationBudgetRequiredCodeRule("declaration budget needs a value"),
            DeclarationBudgetCodePrefixRule("REF"),
        ),
    )
}
