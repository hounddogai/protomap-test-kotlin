package com.acme.transport.crewing.swap.swapschedule

/** A rule a adaptive swap schedule must pass. Check returns its problems, or none. */
interface AdaptiveSwapScheduleRule {
    fun check(item: AdaptiveSwapSchedule): MutableList<String>
}

data class AdaptiveSwapScheduleCodeLengthRule(var maxLength: Int) : AdaptiveSwapScheduleRule {
    override fun check(item: AdaptiveSwapSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapScheduleRequiredLabelRule(var message: String) : AdaptiveSwapScheduleRule {
    override fun check(item: AdaptiveSwapSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapScheduleFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveSwapScheduleRule {
    override fun check(item: AdaptiveSwapSchedule): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive swap schedule. */
data class AdaptiveSwapScheduleValidator(var rules: MutableList<AdaptiveSwapScheduleRule>) {
    fun validate(item: AdaptiveSwapSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveSwapSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSwapSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSwapScheduleValidator(): AdaptiveSwapScheduleValidator {
    return AdaptiveSwapScheduleValidator(
        mutableListOf<AdaptiveSwapScheduleRule>(
            AdaptiveSwapScheduleCodeLengthRule(1000),
            AdaptiveSwapScheduleRequiredLabelRule("adaptive swap schedule needs a value"),
            AdaptiveSwapScheduleFuelLitersRangeRule(0.5, 2452.1),
        ),
    )
}
