package com.acme.transport.customs.declaration.budget

/** A rule a strict declaration budget must pass. Check returns its problems, or none. */
interface StrictDeclarationBudgetRule {
    fun check(item: StrictDeclarationBudget): MutableList<String>
}

data class StrictDeclarationBudgetCodePrefixRule(var prefix: String) : StrictDeclarationBudgetRule {
    override fun check(item: StrictDeclarationBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationBudgetRequiredCodeRule(var message: String) : StrictDeclarationBudgetRule {
    override fun check(item: StrictDeclarationBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationBudgetCodeLengthRule(var maxLength: Int) : StrictDeclarationBudgetRule {
    override fun check(item: StrictDeclarationBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict declaration budget. */
data class StrictDeclarationBudgetValidator(var rules: MutableList<StrictDeclarationBudgetRule>) {
    fun validate(item: StrictDeclarationBudget): MutableList<String> {
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

    fun requireValid(item: StrictDeclarationBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDeclarationBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDeclarationBudgetValidator(): StrictDeclarationBudgetValidator {
    return StrictDeclarationBudgetValidator(
        mutableListOf<StrictDeclarationBudgetRule>(
            StrictDeclarationBudgetCodePrefixRule("X"),
            StrictDeclarationBudgetRequiredCodeRule("strict declaration budget needs a value"),
            StrictDeclarationBudgetCodeLengthRule(1000),
        ),
    )
}
