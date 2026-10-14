package com.acme.transport.terminal.appointment.baseline

/** A rule a adaptive appointment budget must pass. Check returns its problems, or none. */
interface AdaptiveAppointmentBudgetRule {
    fun check(item: AdaptiveAppointmentBudget): MutableList<String>
}

data class AdaptiveAppointmentBudgetEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveAppointmentBudgetRule {
    override fun check(item: AdaptiveAppointmentBudget): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentBudgetDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveAppointmentBudgetRule {
    override fun check(item: AdaptiveAppointmentBudget): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentBudgetNameLengthRule(var maxLength: Int) : AdaptiveAppointmentBudgetRule {
    override fun check(item: AdaptiveAppointmentBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentBudgetRequiredCodeRule(var message: String) : AdaptiveAppointmentBudgetRule {
    override fun check(item: AdaptiveAppointmentBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentBudgetQuantityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveAppointmentBudgetRule {
    override fun check(item: AdaptiveAppointmentBudget): MutableList<String> {
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

data class AdaptiveAppointmentBudgetRequiredTitleRule(var message: String) : AdaptiveAppointmentBudgetRule {
    override fun check(item: AdaptiveAppointmentBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive appointment budget. */
data class AdaptiveAppointmentBudgetValidator(var rules: MutableList<AdaptiveAppointmentBudgetRule>) {
    fun validate(item: AdaptiveAppointmentBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAppointmentBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAppointmentBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAppointmentBudgetValidator(): AdaptiveAppointmentBudgetValidator {
    return AdaptiveAppointmentBudgetValidator(
        mutableListOf<AdaptiveAppointmentBudgetRule>(
            AdaptiveAppointmentBudgetEndsAtRangeRule(255, 3600),
            AdaptiveAppointmentBudgetDistanceKmRangeRule(0.6, 3727.2),
            AdaptiveAppointmentBudgetNameLengthRule(80),
            AdaptiveAppointmentBudgetRequiredCodeRule("adaptive appointment budget needs this field"),
            AdaptiveAppointmentBudgetQuantityRangeRule(255, 3600),
            AdaptiveAppointmentBudgetRequiredTitleRule("adaptive appointment budget needs an entry"),
        ),
    )
}
