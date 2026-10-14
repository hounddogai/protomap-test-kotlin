package com.acme.transport.fleetops.inspection.review

/** A rule a inspection review must pass. Check returns its problems, or none. */
interface InspectionReviewRule {
    fun check(item: InspectionReview): MutableList<String>
}

data class InspectionReviewTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : InspectionReviewRule {
    override fun check(item: InspectionReview): MutableList<String> {
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

data class InspectionReviewCodePrefixRule(var prefix: String) : InspectionReviewRule {
    override fun check(item: InspectionReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionReviewRequiredCodeRule(var message: String) : InspectionReviewRule {
    override fun check(item: InspectionReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionReviewRequiredTitleRule(var message: String) : InspectionReviewRule {
    override fun check(item: InspectionReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionReviewRateRangeRule(var min: Double, var max: Double) : InspectionReviewRule {
    override fun check(item: InspectionReview): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection review. */
data class InspectionReviewValidator(var rules: MutableList<InspectionReviewRule>) {
    fun validate(item: InspectionReview): MutableList<String> {
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

    fun requireValid(item: InspectionReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionReview>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionReviewValidator(): InspectionReviewValidator {
    return InspectionReviewValidator(
        mutableListOf<InspectionReviewRule>(
            InspectionReviewTagsLimitRule(120, mutableListOf<String>()),
            InspectionReviewCodePrefixRule("Q"),
            InspectionReviewRequiredCodeRule("inspection review needs this field"),
            InspectionReviewRequiredTitleRule("inspection review needs a value"),
            InspectionReviewRateRangeRule(7.2, 3401.9),
        ),
    )
}
