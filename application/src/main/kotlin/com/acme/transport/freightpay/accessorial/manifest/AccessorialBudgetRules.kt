package com.acme.transport.freightpay.accessorial.manifest

/** A rule a accessorial budget must pass. Check returns its problems, or none. */
interface AccessorialBudgetRule {
    fun check(item: AccessorialBudget): MutableList<String>
}

data class AccessorialBudgetCodeLengthRule(var maxLength: Int) : AccessorialBudgetRule {
    override fun check(item: AccessorialBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBudgetRequiredCurrencyRule(var message: String) : AccessorialBudgetRule {
    override fun check(item: AccessorialBudget): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBudgetCurrencyLengthRule(var maxLength: Int) : AccessorialBudgetRule {
    override fun check(item: AccessorialBudget): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBudgetCostRangeRule(var min: Double, var max: Double) : AccessorialBudgetRule {
    override fun check(item: AccessorialBudget): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial budget. */
data class AccessorialBudgetValidator(var rules: MutableList<AccessorialBudgetRule>) {
    fun validate(item: AccessorialBudget): MutableList<String> {
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

    fun requireValid(item: AccessorialBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialBudgetValidator(): AccessorialBudgetValidator {
    return AccessorialBudgetValidator(
        mutableListOf<AccessorialBudgetRule>(
            AccessorialBudgetCodeLengthRule(86400),
            AccessorialBudgetRequiredCurrencyRule("accessorial budget needs an entry"),
            AccessorialBudgetCurrencyLengthRule(120),
            AccessorialBudgetCostRangeRule(5.9, 2655.0),
        ),
    )
}
