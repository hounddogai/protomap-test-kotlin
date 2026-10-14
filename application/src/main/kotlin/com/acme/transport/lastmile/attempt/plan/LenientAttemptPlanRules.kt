package com.acme.transport.lastmile.attempt.plan

/** A rule a lenient attempt plan must pass. Check returns its problems, or none. */
interface LenientAttemptPlanRule {
    fun check(item: LenientAttemptPlan): MutableList<String>
}

data class LenientAttemptPlanCodeLengthRule(var maxLength: Int) : LenientAttemptPlanRule {
    override fun check(item: LenientAttemptPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptPlanStartsAtRangeRule(var min: Int, var max: Int) : LenientAttemptPlanRule {
    override fun check(item: LenientAttemptPlan): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptPlanRequiredNameRule(var message: String) : LenientAttemptPlanRule {
    override fun check(item: LenientAttemptPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptPlanCodePrefixRule(var prefix: String) : LenientAttemptPlanRule {
    override fun check(item: LenientAttemptPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptPlanRequiredTimezoneRule(var message: String) : LenientAttemptPlanRule {
    override fun check(item: LenientAttemptPlan): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient attempt plan. */
data class LenientAttemptPlanValidator(var rules: MutableList<LenientAttemptPlanRule>) {
    fun validate(item: LenientAttemptPlan): MutableList<String> {
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

    fun requireValid(item: LenientAttemptPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAttemptPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAttemptPlanValidator(): LenientAttemptPlanValidator {
    return LenientAttemptPlanValidator(
        mutableListOf<LenientAttemptPlanRule>(
            LenientAttemptPlanCodeLengthRule(3600),
            LenientAttemptPlanStartsAtRangeRule(64, 255),
            LenientAttemptPlanRequiredNameRule("lenient attempt plan needs an entry"),
            LenientAttemptPlanCodePrefixRule("OPS"),
            LenientAttemptPlanRequiredTimezoneRule("lenient attempt plan needs this field"),
        ),
    )
}
