package com.acme.transport.terminal.appointment.metric

/** A rule a appointment plan must pass. Check returns its problems, or none. */
interface AppointmentPlanRule {
    fun check(item: AppointmentPlan): MutableList<String>
}

data class AppointmentPlanCodeLengthRule(var maxLength: Int) : AppointmentPlanRule {
    override fun check(item: AppointmentPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPlanDiscountRangeRule(var min: Double, var max: Double) : AppointmentPlanRule {
    override fun check(item: AppointmentPlan): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPlanUpdatedAtRangeRule(var min: Int, var max: Int) : AppointmentPlanRule {
    override fun check(item: AppointmentPlan): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPlanTrailerIdLengthRule(var maxLength: Int) : AppointmentPlanRule {
    override fun check(item: AppointmentPlan): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPlanRequiredCodeRule(var message: String) : AppointmentPlanRule {
    override fun check(item: AppointmentPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment plan. */
data class AppointmentPlanValidator(var rules: MutableList<AppointmentPlanRule>) {
    fun validate(item: AppointmentPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentPlanValidator(): AppointmentPlanValidator {
    return AppointmentPlanValidator(
        mutableListOf<AppointmentPlanRule>(
            AppointmentPlanCodeLengthRule(3600),
            AppointmentPlanDiscountRangeRule(1.5, 1352.1),
            AppointmentPlanUpdatedAtRangeRule(16, 80),
            AppointmentPlanTrailerIdLengthRule(3600),
            AppointmentPlanRequiredCodeRule("appointment plan needs this field"),
        ),
    )
}
