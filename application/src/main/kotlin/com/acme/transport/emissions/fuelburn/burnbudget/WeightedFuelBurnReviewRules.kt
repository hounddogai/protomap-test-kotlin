package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.text.normalize

/** A rule a weighted fuel burn review must pass. Check returns its problems, or none. */
interface WeightedFuelBurnReviewRule {
    fun check(item: WeightedFuelBurnReview): MutableList<String>
}

data class WeightedFuelBurnReviewRevisionRangeRule(var min: Int, var max: Int) : WeightedFuelBurnReviewRule {
    override fun check(item: WeightedFuelBurnReview): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class WeightedFuelBurnReviewCodePrefixRule(var prefix: String) : WeightedFuelBurnReviewRule {
    override fun check(item: WeightedFuelBurnReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnReviewAllowedCodeRule(
    var allowed: MutableList<String>,
) : WeightedFuelBurnReviewRule {
    override fun check(item: WeightedFuelBurnReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnReviewRequiredNameRule(var message: String) : WeightedFuelBurnReviewRule {
    override fun check(item: WeightedFuelBurnReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnReviewCodeLengthRule(var maxLength: Int) : WeightedFuelBurnReviewRule {
    override fun check(item: WeightedFuelBurnReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnReviewRequiredCodeRule(var message: String) : WeightedFuelBurnReviewRule {
    override fun check(item: WeightedFuelBurnReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted fuel burn review. */
data class WeightedFuelBurnReviewValidator(var rules: MutableList<WeightedFuelBurnReviewRule>) {
    fun validate(item: WeightedFuelBurnReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedFuelBurnReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFuelBurnReview>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFuelBurnReviewValidator(): WeightedFuelBurnReviewValidator {
    return WeightedFuelBurnReviewValidator(
        mutableListOf<WeightedFuelBurnReviewRule>(
            WeightedFuelBurnReviewRevisionRangeRule(120, 255),
            WeightedFuelBurnReviewCodePrefixRule("Q"),
            WeightedFuelBurnReviewAllowedCodeRule(mutableListOf<String>("tiered")),
            WeightedFuelBurnReviewRequiredNameRule("weighted fuel burn review needs a value"),
            WeightedFuelBurnReviewCodeLengthRule(120),
            WeightedFuelBurnReviewRequiredCodeRule("weighted fuel burn review needs a value"),
        ),
    )
}
