package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.kit.text.normalize

/** A rule a lenient service area review must pass. Check returns its problems, or none. */
interface LenientServiceAreaReviewRule {
    fun check(item: LenientServiceAreaReview): MutableList<String>
}

data class LenientServiceAreaReviewRequiredSummaryRule(var message: String) : LenientServiceAreaReviewRule {
    override fun check(item: LenientServiceAreaReview): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaReviewAllowedLabelRule(
    var allowed: MutableList<String>,
) : LenientServiceAreaReviewRule {
    override fun check(item: LenientServiceAreaReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaReviewCodeLengthRule(var maxLength: Int) : LenientServiceAreaReviewRule {
    override fun check(item: LenientServiceAreaReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaReviewSummaryLengthRule(var maxLength: Int) : LenientServiceAreaReviewRule {
    override fun check(item: LenientServiceAreaReview): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaReviewWeightRangeRule(
    var min: Double,
    var max: Double,
) : LenientServiceAreaReviewRule {
    override fun check(item: LenientServiceAreaReview): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient service area review. */
data class LenientServiceAreaReviewValidator(var rules: MutableList<LenientServiceAreaReviewRule>) {
    fun validate(item: LenientServiceAreaReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientServiceAreaReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientServiceAreaReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientServiceAreaReviewValidator(): LenientServiceAreaReviewValidator {
    return LenientServiceAreaReviewValidator(
        mutableListOf<LenientServiceAreaReviewRule>(
            LenientServiceAreaReviewRequiredSummaryRule("lenient service area review needs this field"),
            LenientServiceAreaReviewAllowedLabelRule(mutableListOf<String>("pending")),
            LenientServiceAreaReviewCodeLengthRule(120),
            LenientServiceAreaReviewSummaryLengthRule(16),
            LenientServiceAreaReviewWeightRangeRule(2.2, 2815.6),
        ),
    )
}
