package com.acme.transport.fleetops.leasereturn.entry

/** A rule a primary lease return budget must pass. Check returns its problems, or none. */
interface PrimaryLeaseReturnBudgetRule {
    fun check(item: PrimaryLeaseReturnBudget): MutableList<String>
}

data class PrimaryLeaseReturnBudgetCurrencyLengthRule(var maxLength: Int) : PrimaryLeaseReturnBudgetRule {
    override fun check(item: PrimaryLeaseReturnBudget): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLeaseReturnBudgetRequiredTitleRule(var message: String) : PrimaryLeaseReturnBudgetRule {
    override fun check(item: PrimaryLeaseReturnBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLeaseReturnBudgetRateRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryLeaseReturnBudgetRule {
    override fun check(item: PrimaryLeaseReturnBudget): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class PrimaryLeaseReturnBudgetRequiredCodeRule(var message: String) : PrimaryLeaseReturnBudgetRule {
    override fun check(item: PrimaryLeaseReturnBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary lease return budget. */
data class PrimaryLeaseReturnBudgetValidator(var rules: MutableList<PrimaryLeaseReturnBudgetRule>) {
    fun validate(item: PrimaryLeaseReturnBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryLeaseReturnBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryLeaseReturnBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryLeaseReturnBudgetValidator(): PrimaryLeaseReturnBudgetValidator {
    return PrimaryLeaseReturnBudgetValidator(
        mutableListOf<PrimaryLeaseReturnBudgetRule>(
            PrimaryLeaseReturnBudgetCurrencyLengthRule(64),
            PrimaryLeaseReturnBudgetRequiredTitleRule("primary lease return budget needs a value"),
            PrimaryLeaseReturnBudgetRateRangeRule(4.4, 3466.6),
            PrimaryLeaseReturnBudgetRequiredCodeRule("primary lease return budget needs this field"),
        ),
    )
}
