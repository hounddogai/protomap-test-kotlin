package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.text.normalize

/** A rule a capacity budget must pass. Check returns its problems, or none. */
interface CapacityBudgetRule {
    fun check(item: CapacityBudget): MutableList<String>
}

data class CapacityBudgetNotesLengthRule(var maxLength: Int) : CapacityBudgetRule {
    override fun check(item: CapacityBudget): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityBudgetAmountRangeRule(var min: Double, var max: Double) : CapacityBudgetRule {
    override fun check(item: CapacityBudget): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityBudgetRequiredNotesRule(var message: String) : CapacityBudgetRule {
    override fun check(item: CapacityBudget): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityBudgetDistanceKmRangeRule(var min: Double, var max: Double) : CapacityBudgetRule {
    override fun check(item: CapacityBudget): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityBudgetAllowedTitleRule(var allowed: MutableList<String>) : CapacityBudgetRule {
    override fun check(item: CapacityBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CapacityBudgetRequiredCodeRule(var message: String) : CapacityBudgetRule {
    override fun check(item: CapacityBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity budget. */
data class CapacityBudgetValidator(var rules: MutableList<CapacityBudgetRule>) {
    fun validate(item: CapacityBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityBudgetValidator(): CapacityBudgetValidator {
    return CapacityBudgetValidator(
        mutableListOf<CapacityBudgetRule>(
            CapacityBudgetNotesLengthRule(255),
            CapacityBudgetAmountRangeRule(3.1, 1318.8),
            CapacityBudgetRequiredNotesRule("capacity budget needs this field"),
            CapacityBudgetDistanceKmRangeRule(0.9, 4068.4),
            CapacityBudgetAllowedTitleRule(mutableListOf<String>("rolling")),
            CapacityBudgetRequiredCodeRule("capacity budget needs a value"),
        ),
    )
}
