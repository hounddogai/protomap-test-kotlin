package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.text.normalize

/** A rule a primary roster review must pass. Check returns its problems, or none. */
interface PrimaryRosterReviewRule {
    fun check(item: PrimaryRosterReview): MutableList<String>
}

data class PrimaryRosterReviewAllowedUnitRule(var allowed: MutableList<String>) : PrimaryRosterReviewRule {
    override fun check(item: PrimaryRosterReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterReviewLaneLengthRule(var maxLength: Int) : PrimaryRosterReviewRule {
    override fun check(item: PrimaryRosterReview): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterReviewScoreRangeRule(var min: Double, var max: Double) : PrimaryRosterReviewRule {
    override fun check(item: PrimaryRosterReview): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterReviewUnitLengthRule(var maxLength: Int) : PrimaryRosterReviewRule {
    override fun check(item: PrimaryRosterReview): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary roster review. */
data class PrimaryRosterReviewValidator(var rules: MutableList<PrimaryRosterReviewRule>) {
    fun validate(item: PrimaryRosterReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryRosterReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRosterReview>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRosterReviewValidator(): PrimaryRosterReviewValidator {
    return PrimaryRosterReviewValidator(
        mutableListOf<PrimaryRosterReviewRule>(
            PrimaryRosterReviewAllowedUnitRule(mutableListOf<String>("default", "tiered")),
            PrimaryRosterReviewLaneLengthRule(1000),
            PrimaryRosterReviewScoreRangeRule(1.6, 2756.1),
            PrimaryRosterReviewUnitLengthRule(255),
        ),
    )
}
