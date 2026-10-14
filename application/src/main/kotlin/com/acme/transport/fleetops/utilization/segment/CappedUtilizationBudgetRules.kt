package com.acme.transport.fleetops.utilization.segment

/** A rule a capped utilization budget must pass. Check returns its problems, or none. */
interface CappedUtilizationBudgetRule {
    fun check(item: CappedUtilizationBudget): MutableList<String>
}

data class CappedUtilizationBudgetNameLengthRule(var maxLength: Int) : CappedUtilizationBudgetRule {
    override fun check(item: CappedUtilizationBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedUtilizationBudgetQuantityRangeRule(var min: Int, var max: Int) : CappedUtilizationBudgetRule {
    override fun check(item: CappedUtilizationBudget): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class CappedUtilizationBudgetRequiredCategoryRule(var message: String) : CappedUtilizationBudgetRule {
    override fun check(item: CappedUtilizationBudget): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedUtilizationBudgetRequiredCodeRule(var message: String) : CappedUtilizationBudgetRule {
    override fun check(item: CappedUtilizationBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped utilization budget. */
data class CappedUtilizationBudgetValidator(var rules: MutableList<CappedUtilizationBudgetRule>) {
    fun validate(item: CappedUtilizationBudget): MutableList<String> {
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

    fun requireValid(item: CappedUtilizationBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedUtilizationBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedUtilizationBudgetValidator(): CappedUtilizationBudgetValidator {
    return CappedUtilizationBudgetValidator(
        mutableListOf<CappedUtilizationBudgetRule>(
            CappedUtilizationBudgetNameLengthRule(120),
            CappedUtilizationBudgetQuantityRangeRule(16, 16),
            CappedUtilizationBudgetRequiredCategoryRule("capped utilization budget needs a value"),
            CappedUtilizationBudgetRequiredCodeRule("capped utilization budget needs a value"),
        ),
    )
}
