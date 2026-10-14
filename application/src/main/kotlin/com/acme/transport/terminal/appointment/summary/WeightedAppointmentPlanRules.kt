package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.text.normalize

/** A rule a weighted appointment plan must pass. Check returns its problems, or none. */
interface WeightedAppointmentPlanRule {
    fun check(item: WeightedAppointmentPlan): MutableList<String>
}

data class WeightedAppointmentPlanRequiredCodeRule(var message: String) : WeightedAppointmentPlanRule {
    override fun check(item: WeightedAppointmentPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAppointmentPlanAllowedLaneRule(
    var allowed: MutableList<String>,
) : WeightedAppointmentPlanRule {
    override fun check(item: WeightedAppointmentPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAppointmentPlanPalletsRangeRule(var min: Int, var max: Int) : WeightedAppointmentPlanRule {
    override fun check(item: WeightedAppointmentPlan): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted appointment plan. */
data class WeightedAppointmentPlanValidator(var rules: MutableList<WeightedAppointmentPlanRule>) {
    fun validate(item: WeightedAppointmentPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAppointmentPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAppointmentPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAppointmentPlanValidator(): WeightedAppointmentPlanValidator {
    return WeightedAppointmentPlanValidator(
        mutableListOf<WeightedAppointmentPlanRule>(
            WeightedAppointmentPlanRequiredCodeRule("weighted appointment plan needs an entry"),
            WeightedAppointmentPlanAllowedLaneRule(mutableListOf<String>("weighted", "adaptive", "scheduled")),
            WeightedAppointmentPlanPalletsRangeRule(1000, 3600),
        ),
    )
}
