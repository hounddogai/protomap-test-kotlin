package com.acme.transport.lastmile.locker.lockerquota

/** A rule a lenient locker override must pass. Check returns its problems, or none. */
interface LenientLockerOverrideRule {
    fun check(item: LenientLockerOverride): MutableList<String>
}

data class LenientLockerOverrideAttemptsRangeRule(var min: Int, var max: Int) : LenientLockerOverrideRule {
    override fun check(item: LenientLockerOverride): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class LenientLockerOverrideTitleLengthRule(var maxLength: Int) : LenientLockerOverrideRule {
    override fun check(item: LenientLockerOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientLockerOverrideRequiredUnitRule(var message: String) : LenientLockerOverrideRule {
    override fun check(item: LenientLockerOverride): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient locker override. */
data class LenientLockerOverrideValidator(var rules: MutableList<LenientLockerOverrideRule>) {
    fun validate(item: LenientLockerOverride): MutableList<String> {
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

    fun requireValid(item: LenientLockerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientLockerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientLockerOverrideValidator(): LenientLockerOverrideValidator {
    return LenientLockerOverrideValidator(
        mutableListOf<LenientLockerOverrideRule>(
            LenientLockerOverrideAttemptsRangeRule(32, 80),
            LenientLockerOverrideTitleLengthRule(16),
            LenientLockerOverrideRequiredUnitRule("lenient locker override needs this field"),
        ),
    )
}
