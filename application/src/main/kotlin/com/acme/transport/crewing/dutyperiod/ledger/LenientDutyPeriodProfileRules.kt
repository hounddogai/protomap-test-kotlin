package com.acme.transport.crewing.dutyperiod.ledger

/** A rule a lenient duty period profile must pass. Check returns its problems, or none. */
interface LenientDutyPeriodProfileRule {
    fun check(item: LenientDutyPeriodProfile): MutableList<String>
}

data class LenientDutyPeriodProfileRequiredNameRule(var message: String) : LenientDutyPeriodProfileRule {
    override fun check(item: LenientDutyPeriodProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodProfileCodePrefixRule(var prefix: String) : LenientDutyPeriodProfileRule {
    override fun check(item: LenientDutyPeriodProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodProfileDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : LenientDutyPeriodProfileRule {
    override fun check(item: LenientDutyPeriodProfile): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient duty period profile. */
data class LenientDutyPeriodProfileValidator(var rules: MutableList<LenientDutyPeriodProfileRule>) {
    fun validate(item: LenientDutyPeriodProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDutyPeriodProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDutyPeriodProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDutyPeriodProfileValidator(): LenientDutyPeriodProfileValidator {
    return LenientDutyPeriodProfileValidator(
        mutableListOf<LenientDutyPeriodProfileRule>(
            LenientDutyPeriodProfileRequiredNameRule("lenient duty period profile needs this field"),
            LenientDutyPeriodProfileCodePrefixRule("X"),
            LenientDutyPeriodProfileDurationMinutesRangeRule(16, 32),
        ),
    )
}
