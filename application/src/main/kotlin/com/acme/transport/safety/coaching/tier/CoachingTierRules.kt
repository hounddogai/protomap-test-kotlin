package com.acme.transport.safety.coaching.tier

/** A rule a coaching tier must pass. Check returns its problems, or none. */
interface CoachingTierRule {
    fun check(item: CoachingTier): MutableList<String>
}

data class CoachingTierRequiredLocaleRule(var message: String) : CoachingTierRule {
    override fun check(item: CoachingTier): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingTierLocaleLengthRule(var maxLength: Int) : CoachingTierRule {
    override fun check(item: CoachingTier): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingTierRequiredCodeRule(var message: String) : CoachingTierRule {
    override fun check(item: CoachingTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingTierMaximumRangeRule(var min: Double, var max: Double) : CoachingTierRule {
    override fun check(item: CoachingTier): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching tier. */
data class CoachingTierValidator(var rules: MutableList<CoachingTierRule>) {
    fun validate(item: CoachingTier): MutableList<String> {
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

    fun requireValid(item: CoachingTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingTier>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingTierValidator(): CoachingTierValidator {
    return CoachingTierValidator(
        mutableListOf<CoachingTierRule>(
            CoachingTierRequiredLocaleRule("coaching tier needs an entry"),
            CoachingTierLocaleLengthRule(255),
            CoachingTierRequiredCodeRule("coaching tier needs a value"),
            CoachingTierMaximumRangeRule(0.7, 4429.6),
        ),
    )
}
