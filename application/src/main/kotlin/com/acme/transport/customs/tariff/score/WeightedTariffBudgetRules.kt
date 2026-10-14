package com.acme.transport.customs.tariff.score

/** A rule a weighted tariff budget must pass. Check returns its problems, or none. */
interface WeightedTariffBudgetRule {
    fun check(item: WeightedTariffBudget): MutableList<String>
}

data class WeightedTariffBudgetCodeLengthRule(var maxLength: Int) : WeightedTariffBudgetRule {
    override fun check(item: WeightedTariffBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffBudgetRequiredTitleRule(var message: String) : WeightedTariffBudgetRule {
    override fun check(item: WeightedTariffBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffBudgetNotesLengthRule(var maxLength: Int) : WeightedTariffBudgetRule {
    override fun check(item: WeightedTariffBudget): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffBudgetBudgetRangeRule(var min: Double, var max: Double) : WeightedTariffBudgetRule {
    override fun check(item: WeightedTariffBudget): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted tariff budget. */
data class WeightedTariffBudgetValidator(var rules: MutableList<WeightedTariffBudgetRule>) {
    fun validate(item: WeightedTariffBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTariffBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTariffBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTariffBudgetValidator(): WeightedTariffBudgetValidator {
    return WeightedTariffBudgetValidator(
        mutableListOf<WeightedTariffBudgetRule>(
            WeightedTariffBudgetCodeLengthRule(64),
            WeightedTariffBudgetRequiredTitleRule("weighted tariff budget needs an entry"),
            WeightedTariffBudgetNotesLengthRule(120),
            WeightedTariffBudgetBudgetRangeRule(3.8, 4004.3),
        ),
    )
}
