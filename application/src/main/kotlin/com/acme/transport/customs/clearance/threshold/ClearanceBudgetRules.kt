package com.acme.transport.customs.clearance.threshold

/** A rule a clearance budget must pass. Check returns its problems, or none. */
interface ClearanceBudgetRule {
    fun check(item: ClearanceBudget): MutableList<String>
}

data class ClearanceBudgetFloorValueRangeRule(var min: Double, var max: Double) : ClearanceBudgetRule {
    override fun check(item: ClearanceBudget): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBudgetCodeLengthRule(var maxLength: Int) : ClearanceBudgetRule {
    override fun check(item: ClearanceBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBudgetThresholdRangeRule(var min: Double, var max: Double) : ClearanceBudgetRule {
    override fun check(item: ClearanceBudget): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class ClearanceBudgetBudgetRangeRule(var min: Double, var max: Double) : ClearanceBudgetRule {
    override fun check(item: ClearanceBudget): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBudgetRequiredDepotRule(var message: String) : ClearanceBudgetRule {
    override fun check(item: ClearanceBudget): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance budget. */
data class ClearanceBudgetValidator(var rules: MutableList<ClearanceBudgetRule>) {
    fun validate(item: ClearanceBudget): MutableList<String> {
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

    fun requireValid(item: ClearanceBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceBudgetValidator(): ClearanceBudgetValidator {
    return ClearanceBudgetValidator(
        mutableListOf<ClearanceBudgetRule>(
            ClearanceBudgetFloorValueRangeRule(1.6, 657.6),
            ClearanceBudgetCodeLengthRule(120),
            ClearanceBudgetThresholdRangeRule(0.4, 3805.6),
            ClearanceBudgetBudgetRangeRule(5.9, 471.9),
            ClearanceBudgetRequiredDepotRule("clearance budget needs an entry"),
        ),
    )
}
