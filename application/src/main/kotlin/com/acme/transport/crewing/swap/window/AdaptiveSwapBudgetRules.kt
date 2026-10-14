package com.acme.transport.crewing.swap.window

/** A rule a adaptive swap budget must pass. Check returns its problems, or none. */
interface AdaptiveSwapBudgetRule {
    fun check(item: AdaptiveSwapBudget): MutableList<String>
}

data class AdaptiveSwapBudgetCodeLengthRule(var maxLength: Int) : AdaptiveSwapBudgetRule {
    override fun check(item: AdaptiveSwapBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapBudgetTitleLengthRule(var maxLength: Int) : AdaptiveSwapBudgetRule {
    override fun check(item: AdaptiveSwapBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapBudgetRequiredDriverIdRule(var message: String) : AdaptiveSwapBudgetRule {
    override fun check(item: AdaptiveSwapBudget): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapBudgetRequiredCodeRule(var message: String) : AdaptiveSwapBudgetRule {
    override fun check(item: AdaptiveSwapBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive swap budget. */
data class AdaptiveSwapBudgetValidator(var rules: MutableList<AdaptiveSwapBudgetRule>) {
    fun validate(item: AdaptiveSwapBudget): MutableList<String> {
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

    fun requireValid(item: AdaptiveSwapBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSwapBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSwapBudgetValidator(): AdaptiveSwapBudgetValidator {
    return AdaptiveSwapBudgetValidator(
        mutableListOf<AdaptiveSwapBudgetRule>(
            AdaptiveSwapBudgetCodeLengthRule(120),
            AdaptiveSwapBudgetTitleLengthRule(1000),
            AdaptiveSwapBudgetRequiredDriverIdRule("adaptive swap budget needs this field"),
            AdaptiveSwapBudgetRequiredCodeRule("adaptive swap budget needs an entry"),
        ),
    )
}
