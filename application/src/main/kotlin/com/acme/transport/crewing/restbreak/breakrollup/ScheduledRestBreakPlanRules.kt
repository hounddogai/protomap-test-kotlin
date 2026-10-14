package com.acme.transport.crewing.restbreak.breakrollup

/** A rule a scheduled rest break plan must pass. Check returns its problems, or none. */
interface ScheduledRestBreakPlanRule {
    fun check(item: ScheduledRestBreakPlan): MutableList<String>
}

data class ScheduledRestBreakPlanPalletsRangeRule(var min: Int, var max: Int) : ScheduledRestBreakPlanRule {
    override fun check(item: ScheduledRestBreakPlan): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakPlanRequiredCodeRule(var message: String) : ScheduledRestBreakPlanRule {
    override fun check(item: ScheduledRestBreakPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakPlanRequiredDriverIdRule(var message: String) : ScheduledRestBreakPlanRule {
    override fun check(item: ScheduledRestBreakPlan): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled rest break plan. */
data class ScheduledRestBreakPlanValidator(var rules: MutableList<ScheduledRestBreakPlanRule>) {
    fun validate(item: ScheduledRestBreakPlan): MutableList<String> {
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

    fun requireValid(item: ScheduledRestBreakPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRestBreakPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRestBreakPlanValidator(): ScheduledRestBreakPlanValidator {
    return ScheduledRestBreakPlanValidator(
        mutableListOf<ScheduledRestBreakPlanRule>(
            ScheduledRestBreakPlanPalletsRangeRule(64, 80),
            ScheduledRestBreakPlanRequiredCodeRule("scheduled rest break plan needs a value"),
            ScheduledRestBreakPlanRequiredDriverIdRule("scheduled rest break plan needs this field"),
        ),
    )
}
