package com.acme.transport.safety.nearmiss.baseline

import com.acme.transport.kit.text.normalize

/** A rule a strict near miss review must pass. Check returns its problems, or none. */
interface StrictNearMissReviewRule {
    fun check(item: StrictNearMissReview): MutableList<String>
}

data class StrictNearMissReviewAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : StrictNearMissReviewRule {
    override fun check(item: StrictNearMissReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissReviewFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : StrictNearMissReviewRule {
    override fun check(item: StrictNearMissReview): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissReviewExternalRefLengthRule(var maxLength: Int) : StrictNearMissReviewRule {
    override fun check(item: StrictNearMissReview): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict near miss review. */
data class StrictNearMissReviewValidator(var rules: MutableList<StrictNearMissReviewRule>) {
    fun validate(item: StrictNearMissReview): MutableList<String> {
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

    fun requireValid(item: StrictNearMissReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictNearMissReview>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictNearMissReviewValidator(): StrictNearMissReviewValidator {
    return StrictNearMissReviewValidator(
        mutableListOf<StrictNearMissReviewRule>(
            StrictNearMissReviewAllowedExternalRefRule(mutableListOf<String>()),
            StrictNearMissReviewFloorValueRangeRule(1.0, 3537.5),
            StrictNearMissReviewExternalRefLengthRule(120),
        ),
    )
}
