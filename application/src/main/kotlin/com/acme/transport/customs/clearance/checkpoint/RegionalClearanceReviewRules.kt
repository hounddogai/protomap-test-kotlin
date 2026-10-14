package com.acme.transport.customs.clearance.checkpoint

/** A rule a regional clearance review must pass. Check returns its problems, or none. */
interface RegionalClearanceReviewRule {
    fun check(item: RegionalClearanceReview): MutableList<String>
}

data class RegionalClearanceReviewRequiredDriverIdRule(var message: String) : RegionalClearanceReviewRule {
    override fun check(item: RegionalClearanceReview): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceReviewScoreRangeRule(
    var min: Double,
    var max: Double,
) : RegionalClearanceReviewRule {
    override fun check(item: RegionalClearanceReview): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceReviewCodeLengthRule(var maxLength: Int) : RegionalClearanceReviewRule {
    override fun check(item: RegionalClearanceReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceReviewExpiresAtRangeRule(var min: Int, var max: Int) : RegionalClearanceReviewRule {
    override fun check(item: RegionalClearanceReview): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceReviewRequiredLabelRule(var message: String) : RegionalClearanceReviewRule {
    override fun check(item: RegionalClearanceReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceReviewTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RegionalClearanceReviewRule {
    override fun check(item: RegionalClearanceReview): MutableList<String> {
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

/** Runs every rule of a regional clearance review. */
data class RegionalClearanceReviewValidator(var rules: MutableList<RegionalClearanceReviewRule>) {
    fun validate(item: RegionalClearanceReview): MutableList<String> {
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

    fun requireValid(item: RegionalClearanceReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalClearanceReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalClearanceReviewValidator(): RegionalClearanceReviewValidator {
    return RegionalClearanceReviewValidator(
        mutableListOf<RegionalClearanceReviewRule>(
            RegionalClearanceReviewRequiredDriverIdRule("regional clearance review needs a value"),
            RegionalClearanceReviewScoreRangeRule(0.9, 1065.3),
            RegionalClearanceReviewCodeLengthRule(86400),
            RegionalClearanceReviewExpiresAtRangeRule(3600, 3600),
            RegionalClearanceReviewRequiredLabelRule("regional clearance review needs this field"),
            RegionalClearanceReviewTagsLimitRule(16, mutableListOf<String>()),
        ),
    )
}
