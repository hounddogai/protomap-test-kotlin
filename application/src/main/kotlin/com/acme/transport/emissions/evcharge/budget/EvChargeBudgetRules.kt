package com.acme.transport.emissions.evcharge.budget

/** A rule a ev charge budget must pass. Check returns its problems, or none. */
interface EvChargeBudgetRule {
    fun check(item: EvChargeBudget): MutableList<String>
}

data class EvChargeBudgetCodePrefixRule(var prefix: String) : EvChargeBudgetRule {
    override fun check(item: EvChargeBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeBudgetApprovedGuardRule(var limit: Double) : EvChargeBudgetRule {
    override fun check(item: EvChargeBudget): MutableList<String> {
        if (item.approved && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeBudgetGraceDaysRangeRule(var min: Int, var max: Int) : EvChargeBudgetRule {
    override fun check(item: EvChargeBudget): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge budget. */
data class EvChargeBudgetValidator(var rules: MutableList<EvChargeBudgetRule>) {
    fun validate(item: EvChargeBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeBudgetValidator(): EvChargeBudgetValidator {
    return EvChargeBudgetValidator(
        mutableListOf<EvChargeBudgetRule>(
            EvChargeBudgetCodePrefixRule("X"),
            EvChargeBudgetApprovedGuardRule(7.8),
            EvChargeBudgetGraceDaysRangeRule(1000, 3600),
        ),
    )
}
