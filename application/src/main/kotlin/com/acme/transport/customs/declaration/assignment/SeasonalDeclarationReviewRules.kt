package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.text.normalize

/** A rule a seasonal declaration review must pass. Check returns its problems, or none. */
interface SeasonalDeclarationReviewRule {
    fun check(item: SeasonalDeclarationReview): MutableList<String>
}

data class SeasonalDeclarationReviewAllowedUnitRule(
    var allowed: MutableList<String>,
) : SeasonalDeclarationReviewRule {
    override fun check(item: SeasonalDeclarationReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationReviewRateRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalDeclarationReviewRule {
    override fun check(item: SeasonalDeclarationReview): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class SeasonalDeclarationReviewRequiredCodeRule(var message: String) : SeasonalDeclarationReviewRule {
    override fun check(item: SeasonalDeclarationReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationReviewLimitRangeRule(var min: Int, var max: Int) : SeasonalDeclarationReviewRule {
    override fun check(item: SeasonalDeclarationReview): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationReviewStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalDeclarationReviewRule {
    override fun check(item: SeasonalDeclarationReview): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal declaration review. */
data class SeasonalDeclarationReviewValidator(var rules: MutableList<SeasonalDeclarationReviewRule>) {
    fun validate(item: SeasonalDeclarationReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDeclarationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDeclarationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDeclarationReviewValidator(): SeasonalDeclarationReviewValidator {
    return SeasonalDeclarationReviewValidator(
        mutableListOf<SeasonalDeclarationReviewRule>(
            SeasonalDeclarationReviewAllowedUnitRule(mutableListOf<String>("lenient")),
            SeasonalDeclarationReviewRateRangeRule(2.6, 4847.8),
            SeasonalDeclarationReviewRequiredCodeRule("seasonal declaration review needs a value"),
            SeasonalDeclarationReviewLimitRangeRule(1000, 1000),
            SeasonalDeclarationReviewStartsAtRangeRule(80, 80),
        ),
    )
}
