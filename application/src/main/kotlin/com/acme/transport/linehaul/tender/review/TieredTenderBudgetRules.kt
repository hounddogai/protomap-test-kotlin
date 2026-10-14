package com.acme.transport.linehaul.tender.review

/** A rule a tiered tender budget must pass. Check returns its problems, or none. */
interface TieredTenderBudgetRule {
    fun check(item: TieredTenderBudget): MutableList<String>
}

data class TieredTenderBudgetCapacityRangeRule(var min: Int, var max: Int) : TieredTenderBudgetRule {
    override fun check(item: TieredTenderBudget): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class TieredTenderBudgetCodePrefixRule(var prefix: String) : TieredTenderBudgetRule {
    override fun check(item: TieredTenderBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTenderBudgetRequiredNameRule(var message: String) : TieredTenderBudgetRule {
    override fun check(item: TieredTenderBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered tender budget. */
data class TieredTenderBudgetValidator(var rules: MutableList<TieredTenderBudgetRule>) {
    fun validate(item: TieredTenderBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredTenderBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTenderBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTenderBudgetValidator(): TieredTenderBudgetValidator {
    return TieredTenderBudgetValidator(
        mutableListOf<TieredTenderBudgetRule>(
            TieredTenderBudgetCapacityRangeRule(16, 255),
            TieredTenderBudgetCodePrefixRule("Q"),
            TieredTenderBudgetRequiredNameRule("tiered tender budget needs a value"),
        ),
    )
}
