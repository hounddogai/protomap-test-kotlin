package com.acme.transport.linehaul.trailer.claim

/** A rule a lenient trailer variance must pass. Check returns its problems, or none. */
interface LenientTrailerVarianceRule {
    fun check(item: LenientTrailerVariance): MutableList<String>
}

data class LenientTrailerVarianceRequiredCodeRule(var message: String) : LenientTrailerVarianceRule {
    override fun check(item: LenientTrailerVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTrailerVarianceLimitRangeRule(var min: Int, var max: Int) : LenientTrailerVarianceRule {
    override fun check(item: LenientTrailerVariance): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LenientTrailerVarianceGraceDaysRangeRule(var min: Int, var max: Int) : LenientTrailerVarianceRule {
    override fun check(item: LenientTrailerVariance): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientTrailerVarianceRequiredCurrencyRule(var message: String) : LenientTrailerVarianceRule {
    override fun check(item: LenientTrailerVariance): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTrailerVarianceTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LenientTrailerVarianceRule {
    override fun check(item: LenientTrailerVariance): MutableList<String> {
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

data class LenientTrailerVarianceNameLengthRule(var maxLength: Int) : LenientTrailerVarianceRule {
    override fun check(item: LenientTrailerVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient trailer variance. */
data class LenientTrailerVarianceValidator(var rules: MutableList<LenientTrailerVarianceRule>) {
    fun validate(item: LenientTrailerVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientTrailerVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTrailerVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTrailerVarianceValidator(): LenientTrailerVarianceValidator {
    return LenientTrailerVarianceValidator(
        mutableListOf<LenientTrailerVarianceRule>(
            LenientTrailerVarianceRequiredCodeRule("lenient trailer variance needs an entry"),
            LenientTrailerVarianceLimitRangeRule(16, 64),
            LenientTrailerVarianceGraceDaysRangeRule(120, 86400),
            LenientTrailerVarianceRequiredCurrencyRule("lenient trailer variance needs a value"),
            LenientTrailerVarianceTagsLimitRule(32, mutableListOf<String>("primary")),
            LenientTrailerVarianceNameLengthRule(16),
        ),
    )
}
