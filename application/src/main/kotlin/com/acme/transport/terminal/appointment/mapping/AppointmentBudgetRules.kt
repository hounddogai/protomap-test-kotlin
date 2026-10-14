package com.acme.transport.terminal.appointment.mapping

/** A rule a appointment budget must pass. Check returns its problems, or none. */
interface AppointmentBudgetRule {
    fun check(item: AppointmentBudget): MutableList<String>
}

data class AppointmentBudgetDescriptionLengthRule(var maxLength: Int) : AppointmentBudgetRule {
    override fun check(item: AppointmentBudget): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBudgetStartsAtRangeRule(var min: Int, var max: Int) : AppointmentBudgetRule {
    override fun check(item: AppointmentBudget): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class AppointmentBudgetRequiredCodeRule(var message: String) : AppointmentBudgetRule {
    override fun check(item: AppointmentBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBudgetCeilingRangeRule(var min: Double, var max: Double) : AppointmentBudgetRule {
    override fun check(item: AppointmentBudget): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBudgetDriverIdLengthRule(var maxLength: Int) : AppointmentBudgetRule {
    override fun check(item: AppointmentBudget): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment budget. */
data class AppointmentBudgetValidator(var rules: MutableList<AppointmentBudgetRule>) {
    fun validate(item: AppointmentBudget): MutableList<String> {
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

    fun requireValid(item: AppointmentBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentBudgetValidator(): AppointmentBudgetValidator {
    return AppointmentBudgetValidator(
        mutableListOf<AppointmentBudgetRule>(
            AppointmentBudgetDescriptionLengthRule(16),
            AppointmentBudgetStartsAtRangeRule(32, 86400),
            AppointmentBudgetRequiredCodeRule("appointment budget needs a value"),
            AppointmentBudgetCeilingRangeRule(2.6, 2179.6),
            AppointmentBudgetDriverIdLengthRule(1000),
        ),
    )
}
