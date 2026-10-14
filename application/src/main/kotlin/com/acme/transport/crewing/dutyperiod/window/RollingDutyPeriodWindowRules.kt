package com.acme.transport.crewing.dutyperiod.window

/** A rule a rolling duty period window must pass. Check returns its problems, or none. */
interface RollingDutyPeriodWindowRule {
    fun check(item: RollingDutyPeriodWindow): MutableList<String>
}

data class RollingDutyPeriodWindowNameLengthRule(var maxLength: Int) : RollingDutyPeriodWindowRule {
    override fun check(item: RollingDutyPeriodWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodWindowFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RollingDutyPeriodWindowRule {
    override fun check(item: RollingDutyPeriodWindow): MutableList<String> {
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

data class RollingDutyPeriodWindowRequiredNameRule(var message: String) : RollingDutyPeriodWindowRule {
    override fun check(item: RollingDutyPeriodWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling duty period window. */
data class RollingDutyPeriodWindowValidator(var rules: MutableList<RollingDutyPeriodWindowRule>) {
    fun validate(item: RollingDutyPeriodWindow): MutableList<String> {
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

    fun requireValid(item: RollingDutyPeriodWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDutyPeriodWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDutyPeriodWindowValidator(): RollingDutyPeriodWindowValidator {
    return RollingDutyPeriodWindowValidator(
        mutableListOf<RollingDutyPeriodWindowRule>(
            RollingDutyPeriodWindowNameLengthRule(32),
            RollingDutyPeriodWindowFuelLitersRangeRule(5.0, 3010.2),
            RollingDutyPeriodWindowRequiredNameRule("rolling duty period window needs a value"),
        ),
    )
}
