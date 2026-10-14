package com.acme.transport.safety.nearmiss.preference

/** A rule a lenient near miss schedule must pass. Check returns its problems, or none. */
interface LenientNearMissScheduleRule {
    fun check(item: LenientNearMissSchedule): MutableList<String>
}

data class LenientNearMissScheduleLimitRangeRule(var min: Int, var max: Int) : LenientNearMissScheduleRule {
    override fun check(item: LenientNearMissSchedule): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissScheduleRequiredCodeRule(var message: String) : LenientNearMissScheduleRule {
    override fun check(item: LenientNearMissSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissScheduleRequiredPhoneRule(var message: String) : LenientNearMissScheduleRule {
    override fun check(item: LenientNearMissSchedule): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissScheduleLabelLengthRule(var maxLength: Int) : LenientNearMissScheduleRule {
    override fun check(item: LenientNearMissSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissScheduleDriverIdLengthRule(var maxLength: Int) : LenientNearMissScheduleRule {
    override fun check(item: LenientNearMissSchedule): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissScheduleRequiredDriverIdRule(var message: String) : LenientNearMissScheduleRule {
    override fun check(item: LenientNearMissSchedule): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient near miss schedule. */
data class LenientNearMissScheduleValidator(var rules: MutableList<LenientNearMissScheduleRule>) {
    fun validate(item: LenientNearMissSchedule): MutableList<String> {
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

    fun requireValid(item: LenientNearMissSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientNearMissSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientNearMissScheduleValidator(): LenientNearMissScheduleValidator {
    return LenientNearMissScheduleValidator(
        mutableListOf<LenientNearMissScheduleRule>(
            LenientNearMissScheduleLimitRangeRule(32, 1000),
            LenientNearMissScheduleRequiredCodeRule("lenient near miss schedule needs this field"),
            LenientNearMissScheduleRequiredPhoneRule("lenient near miss schedule needs an entry"),
            LenientNearMissScheduleLabelLengthRule(1000),
            LenientNearMissScheduleDriverIdLengthRule(64),
            LenientNearMissScheduleRequiredDriverIdRule("lenient near miss schedule needs an entry"),
        ),
    )
}
