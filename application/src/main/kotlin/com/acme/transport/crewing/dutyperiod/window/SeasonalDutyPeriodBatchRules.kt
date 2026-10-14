package com.acme.transport.crewing.dutyperiod.window

/** A rule a seasonal duty period batch must pass. Check returns its problems, or none. */
interface SeasonalDutyPeriodBatchRule {
    fun check(item: SeasonalDutyPeriodBatch): MutableList<String>
}

data class SeasonalDutyPeriodBatchQuantityRangeRule(var min: Int, var max: Int) : SeasonalDutyPeriodBatchRule {
    override fun check(item: SeasonalDutyPeriodBatch): MutableList<String> {
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

data class SeasonalDutyPeriodBatchBudgetRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalDutyPeriodBatchRule {
    override fun check(item: SeasonalDutyPeriodBatch): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class SeasonalDutyPeriodBatchRequiredDescriptionRule(var message: String) : SeasonalDutyPeriodBatchRule {
    override fun check(item: SeasonalDutyPeriodBatch): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDutyPeriodBatchCodePrefixRule(var prefix: String) : SeasonalDutyPeriodBatchRule {
    override fun check(item: SeasonalDutyPeriodBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal duty period batch. */
data class SeasonalDutyPeriodBatchValidator(var rules: MutableList<SeasonalDutyPeriodBatchRule>) {
    fun validate(item: SeasonalDutyPeriodBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDutyPeriodBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDutyPeriodBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDutyPeriodBatchValidator(): SeasonalDutyPeriodBatchValidator {
    return SeasonalDutyPeriodBatchValidator(
        mutableListOf<SeasonalDutyPeriodBatchRule>(
            SeasonalDutyPeriodBatchQuantityRangeRule(32, 255),
            SeasonalDutyPeriodBatchBudgetRangeRule(5.0, 1049.2),
            SeasonalDutyPeriodBatchRequiredDescriptionRule("seasonal duty period batch needs this field"),
            SeasonalDutyPeriodBatchCodePrefixRule("ACM"),
        ),
    )
}
