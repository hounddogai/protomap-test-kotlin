package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.kit.text.normalize

/** A rule a rolling inspection review must pass. Check returns its problems, or none. */
interface RollingInspectionReviewRule {
    fun check(item: RollingInspectionReview): MutableList<String>
}

data class RollingInspectionReviewRequiredNameRule(var message: String) : RollingInspectionReviewRule {
    override fun check(item: RollingInspectionReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionReviewPalletsRangeRule(var min: Int, var max: Int) : RollingInspectionReviewRule {
    override fun check(item: RollingInspectionReview): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class RollingInspectionReviewAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : RollingInspectionReviewRule {
    override fun check(item: RollingInspectionReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionReviewRequiredContactEmailRule(var message: String) : RollingInspectionReviewRule {
    override fun check(item: RollingInspectionReview): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionReviewThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RollingInspectionReviewRule {
    override fun check(item: RollingInspectionReview): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionReviewTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingInspectionReviewRule {
    override fun check(item: RollingInspectionReview): MutableList<String> {
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

/** Runs every rule of a rolling inspection review. */
data class RollingInspectionReviewValidator(var rules: MutableList<RollingInspectionReviewRule>) {
    fun validate(item: RollingInspectionReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingInspectionReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingInspectionReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingInspectionReviewValidator(): RollingInspectionReviewValidator {
    return RollingInspectionReviewValidator(
        mutableListOf<RollingInspectionReviewRule>(
            RollingInspectionReviewRequiredNameRule("rolling inspection review needs a value"),
            RollingInspectionReviewPalletsRangeRule(1000, 86400),
            RollingInspectionReviewAllowedContactEmailRule(mutableListOf<String>("manual", "batched")),
            RollingInspectionReviewRequiredContactEmailRule("rolling inspection review needs a value"),
            RollingInspectionReviewThresholdRangeRule(7.3, 1395.4),
            RollingInspectionReviewTagsLimitRule(32, mutableListOf<String>("batched")),
        ),
    )
}
