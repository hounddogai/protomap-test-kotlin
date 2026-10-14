package com.acme.transport.customs.hold.holdsummary

/** A rule a lenient hold schedule must pass. Check returns its problems, or none. */
interface LenientHoldScheduleRule {
    fun check(item: LenientHoldSchedule): MutableList<String>
}

data class LenientHoldScheduleCodeLengthRule(var maxLength: Int) : LenientHoldScheduleRule {
    override fun check(item: LenientHoldSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientHoldSchedulePalletsRangeRule(var min: Int, var max: Int) : LenientHoldScheduleRule {
    override fun check(item: LenientHoldSchedule): MutableList<String> {
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

data class LenientHoldScheduleRequiredCodeRule(var message: String) : LenientHoldScheduleRule {
    override fun check(item: LenientHoldSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient hold schedule. */
data class LenientHoldScheduleValidator(var rules: MutableList<LenientHoldScheduleRule>) {
    fun validate(item: LenientHoldSchedule): MutableList<String> {
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

    fun requireValid(item: LenientHoldSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientHoldSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientHoldScheduleValidator(): LenientHoldScheduleValidator {
    return LenientHoldScheduleValidator(
        mutableListOf<LenientHoldScheduleRule>(
            LenientHoldScheduleCodeLengthRule(255),
            LenientHoldSchedulePalletsRangeRule(64, 64),
            LenientHoldScheduleRequiredCodeRule("lenient hold schedule needs an entry"),
        ),
    )
}
