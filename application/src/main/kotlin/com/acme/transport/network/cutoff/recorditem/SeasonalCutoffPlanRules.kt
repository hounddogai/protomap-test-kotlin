package com.acme.transport.network.cutoff.recorditem

/** A rule a seasonal cutoff plan must pass. Check returns its problems, or none. */
interface SeasonalCutoffPlanRule {
    fun check(item: SeasonalCutoffPlan): MutableList<String>
}

data class SeasonalCutoffPlanCodeLengthRule(var maxLength: Int) : SeasonalCutoffPlanRule {
    override fun check(item: SeasonalCutoffPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCutoffPlanCapacityRangeRule(var min: Int, var max: Int) : SeasonalCutoffPlanRule {
    override fun check(item: SeasonalCutoffPlan): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCutoffPlanCostRangeRule(var min: Double, var max: Double) : SeasonalCutoffPlanRule {
    override fun check(item: SeasonalCutoffPlan): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class SeasonalCutoffPlanRequiredPhoneRule(var message: String) : SeasonalCutoffPlanRule {
    override fun check(item: SeasonalCutoffPlan): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal cutoff plan. */
data class SeasonalCutoffPlanValidator(var rules: MutableList<SeasonalCutoffPlanRule>) {
    fun validate(item: SeasonalCutoffPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalCutoffPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCutoffPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCutoffPlanValidator(): SeasonalCutoffPlanValidator {
    return SeasonalCutoffPlanValidator(
        mutableListOf<SeasonalCutoffPlanRule>(
            SeasonalCutoffPlanCodeLengthRule(80),
            SeasonalCutoffPlanCapacityRangeRule(64, 255),
            SeasonalCutoffPlanCostRangeRule(9.9, 1392.7),
            SeasonalCutoffPlanRequiredPhoneRule("seasonal cutoff plan needs this field"),
        ),
    )
}
