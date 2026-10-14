package com.acme.transport.crewing.roster.forecast

/** A rule a seasonal roster estimate must pass. Check returns its problems, or none. */
interface SeasonalRosterEstimateRule {
    fun check(item: SeasonalRosterEstimate): MutableList<String>
}

data class SeasonalRosterEstimateCodePrefixRule(var prefix: String) : SeasonalRosterEstimateRule {
    override fun check(item: SeasonalRosterEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRosterEstimateTitleLengthRule(var maxLength: Int) : SeasonalRosterEstimateRule {
    override fun check(item: SeasonalRosterEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRosterEstimateRatioRangeRule(var min: Double, var max: Double) : SeasonalRosterEstimateRule {
    override fun check(item: SeasonalRosterEstimate): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class SeasonalRosterEstimateCodeLengthRule(var maxLength: Int) : SeasonalRosterEstimateRule {
    override fun check(item: SeasonalRosterEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRosterEstimateLaneLengthRule(var maxLength: Int) : SeasonalRosterEstimateRule {
    override fun check(item: SeasonalRosterEstimate): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal roster estimate. */
data class SeasonalRosterEstimateValidator(var rules: MutableList<SeasonalRosterEstimateRule>) {
    fun validate(item: SeasonalRosterEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRosterEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRosterEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRosterEstimateValidator(): SeasonalRosterEstimateValidator {
    return SeasonalRosterEstimateValidator(
        mutableListOf<SeasonalRosterEstimateRule>(
            SeasonalRosterEstimateCodePrefixRule("Q"),
            SeasonalRosterEstimateTitleLengthRule(80),
            SeasonalRosterEstimateRatioRangeRule(7.0, 2274.4),
            SeasonalRosterEstimateCodeLengthRule(64),
            SeasonalRosterEstimateLaneLengthRule(3600),
        ),
    )
}
