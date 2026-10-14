package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.text.normalize

/** A rule a emission factor budget must pass. Check returns its problems, or none. */
interface EmissionFactorBudgetRule {
    fun check(item: EmissionFactorBudget): MutableList<String>
}

data class EmissionFactorBudgetRatioRangeRule(var min: Double, var max: Double) : EmissionFactorBudgetRule {
    override fun check(item: EmissionFactorBudget): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorBudgetAllowedNameRule(var allowed: MutableList<String>) : EmissionFactorBudgetRule {
    override fun check(item: EmissionFactorBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorBudgetRequiredCodeRule(var message: String) : EmissionFactorBudgetRule {
    override fun check(item: EmissionFactorBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorBudgetNameLengthRule(var maxLength: Int) : EmissionFactorBudgetRule {
    override fun check(item: EmissionFactorBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor budget. */
data class EmissionFactorBudgetValidator(var rules: MutableList<EmissionFactorBudgetRule>) {
    fun validate(item: EmissionFactorBudget): MutableList<String> {
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

    fun requireValid(item: EmissionFactorBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorBudgetValidator(): EmissionFactorBudgetValidator {
    return EmissionFactorBudgetValidator(
        mutableListOf<EmissionFactorBudgetRule>(
            EmissionFactorBudgetRatioRangeRule(6.4, 3321.3),
            EmissionFactorBudgetAllowedNameRule(mutableListOf<String>()),
            EmissionFactorBudgetRequiredCodeRule("emission factor budget needs a value"),
            EmissionFactorBudgetNameLengthRule(255),
        ),
    )
}
