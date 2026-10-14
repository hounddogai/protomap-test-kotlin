package com.acme.transport.lastmile.attempt.batchedattemptrequest

/** A rule a seasonal attempt estimate must pass. Check returns its problems, or none. */
interface SeasonalAttemptEstimateRule {
    fun check(item: SeasonalAttemptEstimate): MutableList<String>
}

data class SeasonalAttemptEstimateChannelLengthRule(var maxLength: Int) : SeasonalAttemptEstimateRule {
    override fun check(item: SeasonalAttemptEstimate): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAttemptEstimateRequiredCurrencyRule(var message: String) : SeasonalAttemptEstimateRule {
    override fun check(item: SeasonalAttemptEstimate): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAttemptEstimateUpdatedAtRangeRule(var min: Int, var max: Int) : SeasonalAttemptEstimateRule {
    override fun check(item: SeasonalAttemptEstimate): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class SeasonalAttemptEstimateCodePrefixRule(var prefix: String) : SeasonalAttemptEstimateRule {
    override fun check(item: SeasonalAttemptEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAttemptEstimateRequiredCodeRule(var message: String) : SeasonalAttemptEstimateRule {
    override fun check(item: SeasonalAttemptEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAttemptEstimateTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : SeasonalAttemptEstimateRule {
    override fun check(item: SeasonalAttemptEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a seasonal attempt estimate. */
data class SeasonalAttemptEstimateValidator(var rules: MutableList<SeasonalAttemptEstimateRule>) {
    fun validate(item: SeasonalAttemptEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAttemptEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAttemptEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAttemptEstimateValidator(): SeasonalAttemptEstimateValidator {
    return SeasonalAttemptEstimateValidator(
        mutableListOf<SeasonalAttemptEstimateRule>(
            SeasonalAttemptEstimateChannelLengthRule(86400),
            SeasonalAttemptEstimateRequiredCurrencyRule("seasonal attempt estimate needs a value"),
            SeasonalAttemptEstimateUpdatedAtRangeRule(255, 1000),
            SeasonalAttemptEstimateCodePrefixRule("ACM"),
            SeasonalAttemptEstimateRequiredCodeRule("seasonal attempt estimate needs an entry"),
            SeasonalAttemptEstimateTagsLimitRule(255, mutableListOf<String>()),
        ),
    )
}
