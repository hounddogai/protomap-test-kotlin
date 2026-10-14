package com.acme.transport.crewing.dutyperiod.estimate

/** A rule a duty period plan must pass. Check returns its problems, or none. */
interface DutyPeriodPlanRule {
    fun check(item: DutyPeriodPlan): MutableList<String>
}

data class DutyPeriodPlanUpdatedAtRangeRule(var min: Int, var max: Int) : DutyPeriodPlanRule {
    override fun check(item: DutyPeriodPlan): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodPlanDiscountRangeRule(var min: Double, var max: Double) : DutyPeriodPlanRule {
    override fun check(item: DutyPeriodPlan): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodPlanRequiredCodeRule(var message: String) : DutyPeriodPlanRule {
    override fun check(item: DutyPeriodPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period plan. */
data class DutyPeriodPlanValidator(var rules: MutableList<DutyPeriodPlanRule>) {
    fun validate(item: DutyPeriodPlan): MutableList<String> {
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

    fun requireValid(item: DutyPeriodPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodPlanValidator(): DutyPeriodPlanValidator {
    return DutyPeriodPlanValidator(
        mutableListOf<DutyPeriodPlanRule>(
            DutyPeriodPlanUpdatedAtRangeRule(16, 120),
            DutyPeriodPlanDiscountRangeRule(8.7, 2781.5),
            DutyPeriodPlanRequiredCodeRule("duty period plan needs an entry"),
        ),
    )
}
