package com.acme.transport.network.transittime.budget

/** A rule a transit time budget must pass. Check returns its problems, or none. */
interface TransitTimeBudgetRule {
    fun check(item: TransitTimeBudget): MutableList<String>
}

data class TransitTimeBudgetRequiredTimezoneRule(var message: String) : TransitTimeBudgetRule {
    override fun check(item: TransitTimeBudget): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeBudgetCodePrefixRule(var prefix: String) : TransitTimeBudgetRule {
    override fun check(item: TransitTimeBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeBudgetDockLengthRule(var maxLength: Int) : TransitTimeBudgetRule {
    override fun check(item: TransitTimeBudget): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time budget. */
data class TransitTimeBudgetValidator(var rules: MutableList<TransitTimeBudgetRule>) {
    fun validate(item: TransitTimeBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeBudgetValidator(): TransitTimeBudgetValidator {
    return TransitTimeBudgetValidator(
        mutableListOf<TransitTimeBudgetRule>(
            TransitTimeBudgetRequiredTimezoneRule("transit time budget needs an entry"),
            TransitTimeBudgetCodePrefixRule("X"),
            TransitTimeBudgetDockLengthRule(64),
        ),
    )
}
