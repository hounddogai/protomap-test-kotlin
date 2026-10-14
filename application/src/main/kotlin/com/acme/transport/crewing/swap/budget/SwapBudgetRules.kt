package com.acme.transport.crewing.swap.budget

/** A rule a swap budget must pass. Check returns its problems, or none. */
interface SwapBudgetRule {
    fun check(item: SwapBudget): MutableList<String>
}

data class SwapBudgetDepotLengthRule(var maxLength: Int) : SwapBudgetRule {
    override fun check(item: SwapBudget): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapBudgetCodeLengthRule(var maxLength: Int) : SwapBudgetRule {
    override fun check(item: SwapBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapBudgetNameLengthRule(var maxLength: Int) : SwapBudgetRule {
    override fun check(item: SwapBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapBudgetRequiredDepotRule(var message: String) : SwapBudgetRule {
    override fun check(item: SwapBudget): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapBudgetRequiredNameRule(var message: String) : SwapBudgetRule {
    override fun check(item: SwapBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap budget. */
data class SwapBudgetValidator(var rules: MutableList<SwapBudgetRule>) {
    fun validate(item: SwapBudget): MutableList<String> {
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

    fun requireValid(item: SwapBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapBudgetValidator(): SwapBudgetValidator {
    return SwapBudgetValidator(
        mutableListOf<SwapBudgetRule>(
            SwapBudgetDepotLengthRule(255),
            SwapBudgetCodeLengthRule(255),
            SwapBudgetNameLengthRule(255),
            SwapBudgetRequiredDepotRule("swap budget needs a value"),
            SwapBudgetRequiredNameRule("swap budget needs an entry"),
        ),
    )
}
