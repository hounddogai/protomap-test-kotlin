package com.acme.transport.linehaul.laneplan.budget

/** A rule a lane plan budget must pass. Check returns its problems, or none. */
interface LanePlanBudgetRule {
    fun check(item: LanePlanBudget): MutableList<String>
}

data class LanePlanBudgetQuantityRangeRule(var min: Int, var max: Int) : LanePlanBudgetRule {
    override fun check(item: LanePlanBudget): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanBudgetUpdatedAtRangeRule(var min: Int, var max: Int) : LanePlanBudgetRule {
    override fun check(item: LanePlanBudget): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanBudgetCodeLengthRule(var maxLength: Int) : LanePlanBudgetRule {
    override fun check(item: LanePlanBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan budget. */
data class LanePlanBudgetValidator(var rules: MutableList<LanePlanBudgetRule>) {
    fun validate(item: LanePlanBudget): MutableList<String> {
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

    fun requireValid(item: LanePlanBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanBudgetValidator(): LanePlanBudgetValidator {
    return LanePlanBudgetValidator(
        mutableListOf<LanePlanBudgetRule>(
            LanePlanBudgetQuantityRangeRule(1000, 86400),
            LanePlanBudgetUpdatedAtRangeRule(255, 3600),
            LanePlanBudgetCodeLengthRule(3600),
        ),
    )
}
