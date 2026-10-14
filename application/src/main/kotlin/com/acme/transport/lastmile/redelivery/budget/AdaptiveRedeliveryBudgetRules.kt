package com.acme.transport.lastmile.redelivery.budget

/** A rule a adaptive redelivery budget must pass. Check returns its problems, or none. */
interface AdaptiveRedeliveryBudgetRule {
    fun check(item: AdaptiveRedeliveryBudget): MutableList<String>
}

data class AdaptiveRedeliveryBudgetAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveRedeliveryBudgetRule {
    override fun check(item: AdaptiveRedeliveryBudget): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRedeliveryBudgetCodePrefixRule(var prefix: String) : AdaptiveRedeliveryBudgetRule {
    override fun check(item: AdaptiveRedeliveryBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRedeliveryBudgetLabelLengthRule(var maxLength: Int) : AdaptiveRedeliveryBudgetRule {
    override fun check(item: AdaptiveRedeliveryBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRedeliveryBudgetRequiredCodeRule(var message: String) : AdaptiveRedeliveryBudgetRule {
    override fun check(item: AdaptiveRedeliveryBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive redelivery budget. */
data class AdaptiveRedeliveryBudgetValidator(var rules: MutableList<AdaptiveRedeliveryBudgetRule>) {
    fun validate(item: AdaptiveRedeliveryBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveRedeliveryBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRedeliveryBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRedeliveryBudgetValidator(): AdaptiveRedeliveryBudgetValidator {
    return AdaptiveRedeliveryBudgetValidator(
        mutableListOf<AdaptiveRedeliveryBudgetRule>(
            AdaptiveRedeliveryBudgetAttemptsRangeRule(86400, 86400),
            AdaptiveRedeliveryBudgetCodePrefixRule("REF"),
            AdaptiveRedeliveryBudgetLabelLengthRule(80),
            AdaptiveRedeliveryBudgetRequiredCodeRule("adaptive redelivery budget needs an entry"),
        ),
    )
}
