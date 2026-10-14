package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.text.normalize

/** A rule a regional fuel burn review must pass. Check returns its problems, or none. */
interface RegionalFuelBurnReviewRule {
    fun check(item: RegionalFuelBurnReview): MutableList<String>
}

data class RegionalFuelBurnReviewDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalFuelBurnReviewRule {
    override fun check(item: RegionalFuelBurnReview): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class RegionalFuelBurnReviewRequiredCodeRule(var message: String) : RegionalFuelBurnReviewRule {
    override fun check(item: RegionalFuelBurnReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnReviewTitleLengthRule(var maxLength: Int) : RegionalFuelBurnReviewRule {
    override fun check(item: RegionalFuelBurnReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnReviewAllowedPhoneRule(
    var allowed: MutableList<String>,
) : RegionalFuelBurnReviewRule {
    override fun check(item: RegionalFuelBurnReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional fuel burn review. */
data class RegionalFuelBurnReviewValidator(var rules: MutableList<RegionalFuelBurnReviewRule>) {
    fun validate(item: RegionalFuelBurnReview): MutableList<String> {
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

    fun requireValid(item: RegionalFuelBurnReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelBurnReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelBurnReviewValidator(): RegionalFuelBurnReviewValidator {
    return RegionalFuelBurnReviewValidator(
        mutableListOf<RegionalFuelBurnReviewRule>(
            RegionalFuelBurnReviewDurationMinutesRangeRule(255, 1000),
            RegionalFuelBurnReviewRequiredCodeRule("regional fuel burn review needs an entry"),
            RegionalFuelBurnReviewTitleLengthRule(16),
            RegionalFuelBurnReviewAllowedPhoneRule(mutableListOf<String>("lenient", "capped")),
        ),
    )
}
