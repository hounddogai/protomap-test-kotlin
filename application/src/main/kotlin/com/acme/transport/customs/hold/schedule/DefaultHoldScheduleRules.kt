package com.acme.transport.customs.hold.schedule

/** A rule a default hold schedule must pass. Check returns its problems, or none. */
interface DefaultHoldScheduleRule {
    fun check(item: DefaultHoldSchedule): MutableList<String>
}

data class DefaultHoldScheduleCreatedAtRangeRule(var min: Int, var max: Int) : DefaultHoldScheduleRule {
    override fun check(item: DefaultHoldSchedule): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHoldScheduleCodeLengthRule(var maxLength: Int) : DefaultHoldScheduleRule {
    override fun check(item: DefaultHoldSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHoldScheduleFuelLitersRangeRule(var min: Double, var max: Double) : DefaultHoldScheduleRule {
    override fun check(item: DefaultHoldSchedule): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class DefaultHoldScheduleCapacityRangeRule(var min: Int, var max: Int) : DefaultHoldScheduleRule {
    override fun check(item: DefaultHoldSchedule): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default hold schedule. */
data class DefaultHoldScheduleValidatorExtra(var rules: MutableList<DefaultHoldScheduleRule>) {
    fun validate(item: DefaultHoldSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultHoldSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultHoldSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultHoldScheduleValidator(): DefaultHoldScheduleValidatorExtra {
    return DefaultHoldScheduleValidatorExtra(
        mutableListOf<DefaultHoldScheduleRule>(
            DefaultHoldScheduleCreatedAtRangeRule(64, 120),
            DefaultHoldScheduleCodeLengthRule(32),
            DefaultHoldScheduleFuelLitersRangeRule(5.3, 3732.3),
            DefaultHoldScheduleCapacityRangeRule(80, 86400),
        ),
    )
}
