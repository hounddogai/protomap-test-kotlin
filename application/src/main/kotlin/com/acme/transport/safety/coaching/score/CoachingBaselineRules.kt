package com.acme.transport.safety.coaching.score

/** A rule a coaching baseline must pass. Check returns its problems, or none. */
interface CoachingBaselineRule {
    fun check(item: CoachingBaseline): MutableList<String>
}

data class CoachingBaselineUnitLengthRule(var maxLength: Int) : CoachingBaselineRule {
    override fun check(item: CoachingBaseline): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingBaselineLimitRangeRule(var min: Int, var max: Int) : CoachingBaselineRule {
    override fun check(item: CoachingBaseline): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingBaselineCurrencyLengthRule(var maxLength: Int) : CoachingBaselineRule {
    override fun check(item: CoachingBaseline): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingBaselineScoreRangeRule(var min: Double, var max: Double) : CoachingBaselineRule {
    override fun check(item: CoachingBaseline): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class CoachingBaselineLabelLengthRule(var maxLength: Int) : CoachingBaselineRule {
    override fun check(item: CoachingBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingBaselineRequiredCodeRule(var message: String) : CoachingBaselineRule {
    override fun check(item: CoachingBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching baseline. */
data class CoachingBaselineValidator(var rules: MutableList<CoachingBaselineRule>) {
    fun validate(item: CoachingBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingBaselineValidator(): CoachingBaselineValidator {
    return CoachingBaselineValidator(
        mutableListOf<CoachingBaselineRule>(
            CoachingBaselineUnitLengthRule(255),
            CoachingBaselineLimitRangeRule(64, 120),
            CoachingBaselineCurrencyLengthRule(80),
            CoachingBaselineScoreRangeRule(9.2, 1625.0),
            CoachingBaselineLabelLengthRule(1000),
            CoachingBaselineRequiredCodeRule("coaching baseline needs this field"),
        ),
    )
}
