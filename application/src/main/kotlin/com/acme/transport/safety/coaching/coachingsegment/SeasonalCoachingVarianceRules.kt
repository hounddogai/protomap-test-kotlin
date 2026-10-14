package com.acme.transport.safety.coaching.coachingsegment

/** A rule a seasonal coaching variance must pass. Check returns its problems, or none. */
interface SeasonalCoachingVarianceRule {
    fun check(item: SeasonalCoachingVariance): MutableList<String>
}

data class SeasonalCoachingVarianceTimezoneLengthRule(var maxLength: Int) : SeasonalCoachingVarianceRule {
    override fun check(item: SeasonalCoachingVariance): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingVarianceLabelLengthRule(var maxLength: Int) : SeasonalCoachingVarianceRule {
    override fun check(item: SeasonalCoachingVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingVarianceCodeLengthRule(var maxLength: Int) : SeasonalCoachingVarianceRule {
    override fun check(item: SeasonalCoachingVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingVarianceRequiredCodeRule(var message: String) : SeasonalCoachingVarianceRule {
    override fun check(item: SeasonalCoachingVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingVarianceCodePrefixRule(var prefix: String) : SeasonalCoachingVarianceRule {
    override fun check(item: SeasonalCoachingVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingVarianceThresholdRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalCoachingVarianceRule {
    override fun check(item: SeasonalCoachingVariance): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal coaching variance. */
data class SeasonalCoachingVarianceValidator(var rules: MutableList<SeasonalCoachingVarianceRule>) {
    fun validate(item: SeasonalCoachingVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalCoachingVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCoachingVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCoachingVarianceValidator(): SeasonalCoachingVarianceValidator {
    return SeasonalCoachingVarianceValidator(
        mutableListOf<SeasonalCoachingVarianceRule>(
            SeasonalCoachingVarianceTimezoneLengthRule(3600),
            SeasonalCoachingVarianceLabelLengthRule(255),
            SeasonalCoachingVarianceCodeLengthRule(86400),
            SeasonalCoachingVarianceRequiredCodeRule("seasonal coaching variance needs an entry"),
            SeasonalCoachingVarianceCodePrefixRule("ACM"),
            SeasonalCoachingVarianceThresholdRangeRule(2.1, 1202.7),
        ),
    )
}
