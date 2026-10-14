package com.acme.transport.safety.coaching.review

/** A rule a fallback coaching window must pass. Check returns its problems, or none. */
interface FallbackCoachingWindowRule {
    fun check(item: FallbackCoachingWindow): MutableList<String>
}

data class FallbackCoachingWindowCapacityRangeRule(var min: Int, var max: Int) : FallbackCoachingWindowRule {
    override fun check(item: FallbackCoachingWindow): MutableList<String> {
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

data class FallbackCoachingWindowCeilingRangeRule(
    var min: Double,
    var max: Double,
) : FallbackCoachingWindowRule {
    override fun check(item: FallbackCoachingWindow): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class FallbackCoachingWindowTitleLengthRule(var maxLength: Int) : FallbackCoachingWindowRule {
    override fun check(item: FallbackCoachingWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback coaching window. */
data class FallbackCoachingWindowValidator(var rules: MutableList<FallbackCoachingWindowRule>) {
    fun validate(item: FallbackCoachingWindow): MutableList<String> {
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

    fun requireValid(item: FallbackCoachingWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCoachingWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCoachingWindowValidator(): FallbackCoachingWindowValidator {
    return FallbackCoachingWindowValidator(
        mutableListOf<FallbackCoachingWindowRule>(
            FallbackCoachingWindowCapacityRangeRule(64, 86400),
            FallbackCoachingWindowCeilingRangeRule(6.2, 1536.7),
            FallbackCoachingWindowTitleLengthRule(3600),
        ),
    )
}
