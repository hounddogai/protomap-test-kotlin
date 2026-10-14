package com.acme.transport.terminal.dwell.snapshot

/** A rule a global dwell budget must pass. Check returns its problems, or none. */
interface GlobalDwellBudgetRule {
    fun check(item: GlobalDwellBudget): MutableList<String>
}

data class GlobalDwellBudgetQuantityRangeRule(var min: Int, var max: Int) : GlobalDwellBudgetRule {
    override fun check(item: GlobalDwellBudget): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDwellBudgetRequiredUnitRule(var message: String) : GlobalDwellBudgetRule {
    override fun check(item: GlobalDwellBudget): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDwellBudgetLoadKgRangeRule(var min: Double, var max: Double) : GlobalDwellBudgetRule {
    override fun check(item: GlobalDwellBudget): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDwellBudgetRequiredCodeRule(var message: String) : GlobalDwellBudgetRule {
    override fun check(item: GlobalDwellBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDwellBudgetContactEmailLengthRule(var maxLength: Int) : GlobalDwellBudgetRule {
    override fun check(item: GlobalDwellBudget): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global dwell budget. */
data class GlobalDwellBudgetValidator(var rules: MutableList<GlobalDwellBudgetRule>) {
    fun validate(item: GlobalDwellBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDwellBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDwellBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDwellBudgetValidator(): GlobalDwellBudgetValidator {
    return GlobalDwellBudgetValidator(
        mutableListOf<GlobalDwellBudgetRule>(
            GlobalDwellBudgetQuantityRangeRule(3600, 86400),
            GlobalDwellBudgetRequiredUnitRule("global dwell budget needs a value"),
            GlobalDwellBudgetLoadKgRangeRule(2.6, 3291.6),
            GlobalDwellBudgetRequiredCodeRule("global dwell budget needs a value"),
            GlobalDwellBudgetContactEmailLengthRule(64),
        ),
    )
}
