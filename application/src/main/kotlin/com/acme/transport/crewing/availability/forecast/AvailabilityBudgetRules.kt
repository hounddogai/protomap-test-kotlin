package com.acme.transport.crewing.availability.forecast

import com.acme.transport.kit.text.normalize

/** A rule a availability budget must pass. Check returns its problems, or none. */
interface AvailabilityBudgetRule {
    fun check(item: AvailabilityBudget): MutableList<String>
}

data class AvailabilityBudgetRatioRangeRule(var min: Double, var max: Double) : AvailabilityBudgetRule {
    override fun check(item: AvailabilityBudget): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class AvailabilityBudgetCostRangeRule(var min: Double, var max: Double) : AvailabilityBudgetRule {
    override fun check(item: AvailabilityBudget): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityBudgetCodePrefixRule(var prefix: String) : AvailabilityBudgetRule {
    override fun check(item: AvailabilityBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityBudgetAllowedNameRule(var allowed: MutableList<String>) : AvailabilityBudgetRule {
    override fun check(item: AvailabilityBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability budget. */
data class AvailabilityBudgetValidator(var rules: MutableList<AvailabilityBudgetRule>) {
    fun validate(item: AvailabilityBudget): MutableList<String> {
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

    fun requireValid(item: AvailabilityBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityBudgetValidator(): AvailabilityBudgetValidator {
    return AvailabilityBudgetValidator(
        mutableListOf<AvailabilityBudgetRule>(
            AvailabilityBudgetRatioRangeRule(9.9, 2748.9),
            AvailabilityBudgetCostRangeRule(6.8, 3008.3),
            AvailabilityBudgetCodePrefixRule("ACM"),
            AvailabilityBudgetAllowedNameRule(mutableListOf<String>("primary")),
        ),
    )
}
