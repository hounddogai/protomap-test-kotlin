package com.acme.transport.safety.coaching.review

/** A rule a seasonal coaching budget must pass. Check returns its problems, or none. */
interface SeasonalCoachingBudgetRule {
    fun check(item: SeasonalCoachingBudget): MutableList<String>
}

data class SeasonalCoachingBudgetRequiredOwnerIdRule(var message: String) : SeasonalCoachingBudgetRule {
    override fun check(item: SeasonalCoachingBudget): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingBudgetCodeLengthRule(var maxLength: Int) : SeasonalCoachingBudgetRule {
    override fun check(item: SeasonalCoachingBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingBudgetRequiredCodeRule(var message: String) : SeasonalCoachingBudgetRule {
    override fun check(item: SeasonalCoachingBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingBudgetLabelLengthRule(var maxLength: Int) : SeasonalCoachingBudgetRule {
    override fun check(item: SeasonalCoachingBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingBudgetMarginRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalCoachingBudgetRule {
    override fun check(item: SeasonalCoachingBudget): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal coaching budget. */
data class SeasonalCoachingBudgetValidator(var rules: MutableList<SeasonalCoachingBudgetRule>) {
    fun validate(item: SeasonalCoachingBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalCoachingBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCoachingBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCoachingBudgetValidator(): SeasonalCoachingBudgetValidator {
    return SeasonalCoachingBudgetValidator(
        mutableListOf<SeasonalCoachingBudgetRule>(
            SeasonalCoachingBudgetRequiredOwnerIdRule("seasonal coaching budget needs an entry"),
            SeasonalCoachingBudgetCodeLengthRule(3600),
            SeasonalCoachingBudgetRequiredCodeRule("seasonal coaching budget needs a value"),
            SeasonalCoachingBudgetLabelLengthRule(1000),
            SeasonalCoachingBudgetMarginRangeRule(8.3, 2606.2),
        ),
    )
}
