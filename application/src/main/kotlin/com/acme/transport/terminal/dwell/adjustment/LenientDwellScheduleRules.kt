package com.acme.transport.terminal.dwell.adjustment

/** A rule a lenient dwell schedule must pass. Check returns its problems, or none. */
interface LenientDwellScheduleRule {
    fun check(item: LenientDwellSchedule): MutableList<String>
}

data class LenientDwellScheduleRequiredLabelRule(var message: String) : LenientDwellScheduleRule {
    override fun check(item: LenientDwellSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellScheduleLaneLengthRule(var maxLength: Int) : LenientDwellScheduleRule {
    override fun check(item: LenientDwellSchedule): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellScheduleCodeLengthRule(var maxLength: Int) : LenientDwellScheduleRule {
    override fun check(item: LenientDwellSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellScheduleRequiredCodeRule(var message: String) : LenientDwellScheduleRule {
    override fun check(item: LenientDwellSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellScheduleRateRangeRule(var min: Double, var max: Double) : LenientDwellScheduleRule {
    override fun check(item: LenientDwellSchedule): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellScheduleRatioRangeRule(var min: Double, var max: Double) : LenientDwellScheduleRule {
    override fun check(item: LenientDwellSchedule): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient dwell schedule. */
data class LenientDwellScheduleValidator(var rules: MutableList<LenientDwellScheduleRule>) {
    fun validate(item: LenientDwellSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDwellSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDwellSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDwellScheduleValidator(): LenientDwellScheduleValidator {
    return LenientDwellScheduleValidator(
        mutableListOf<LenientDwellScheduleRule>(
            LenientDwellScheduleRequiredLabelRule("lenient dwell schedule needs this field"),
            LenientDwellScheduleLaneLengthRule(120),
            LenientDwellScheduleCodeLengthRule(16),
            LenientDwellScheduleRequiredCodeRule("lenient dwell schedule needs a value"),
            LenientDwellScheduleRateRangeRule(2.8, 3050.1),
            LenientDwellScheduleRatioRangeRule(1.0, 2143.2),
        ),
    )
}
