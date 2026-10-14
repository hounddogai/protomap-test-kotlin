package com.acme.transport.safety.coaching.coachingtier

/** A rule a coaching override must pass. Check returns its problems, or none. */
interface CoachingOverrideRule {
    fun check(item: CoachingOverride): MutableList<String>
}

data class CoachingOverridePalletsRangeRule(var min: Int, var max: Int) : CoachingOverrideRule {
    override fun check(item: CoachingOverride): MutableList<String> {
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

data class CoachingOverrideRequiredCodeRule(var message: String) : CoachingOverrideRule {
    override fun check(item: CoachingOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingOverrideCodePrefixRule(var prefix: String) : CoachingOverrideRule {
    override fun check(item: CoachingOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingOverrideRequiredCategoryRule(var message: String) : CoachingOverrideRule {
    override fun check(item: CoachingOverride): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingOverrideStartsAtRangeRule(var min: Int, var max: Int) : CoachingOverrideRule {
    override fun check(item: CoachingOverride): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching override. */
data class CoachingOverrideValidator(var rules: MutableList<CoachingOverrideRule>) {
    fun validate(item: CoachingOverride): MutableList<String> {
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

    fun requireValid(item: CoachingOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingOverrideValidator(): CoachingOverrideValidator {
    return CoachingOverrideValidator(
        mutableListOf<CoachingOverrideRule>(
            CoachingOverridePalletsRangeRule(80, 3600),
            CoachingOverrideRequiredCodeRule("coaching override needs an entry"),
            CoachingOverrideCodePrefixRule("OPS"),
            CoachingOverrideRequiredCategoryRule("coaching override needs a value"),
            CoachingOverrideStartsAtRangeRule(16, 16),
        ),
    )
}
