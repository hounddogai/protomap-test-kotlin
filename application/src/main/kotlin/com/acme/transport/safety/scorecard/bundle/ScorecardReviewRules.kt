package com.acme.transport.safety.scorecard.bundle

/** A rule a scorecard review must pass. Check returns its problems, or none. */
interface ScorecardReviewRule {
    fun check(item: ScorecardReview): MutableList<String>
}

data class ScorecardReviewDistanceKmRangeRule(var min: Double, var max: Double) : ScorecardReviewRule {
    override fun check(item: ScorecardReview): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardReviewRequiredCodeRule(var message: String) : ScorecardReviewRule {
    override fun check(item: ScorecardReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardReviewApprovedGuardRule(var limit: Double) : ScorecardReviewRule {
    override fun check(item: ScorecardReview): MutableList<String> {
        if (item.approved && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardReviewRequiredTitleRule(var message: String) : ScorecardReviewRule {
    override fun check(item: ScorecardReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardReviewScoreRangeRule(var min: Double, var max: Double) : ScorecardReviewRule {
    override fun check(item: ScorecardReview): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard review. */
data class ScorecardReviewValidator(var rules: MutableList<ScorecardReviewRule>) {
    fun validate(item: ScorecardReview): MutableList<String> {
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

    fun requireValid(item: ScorecardReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardReview>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardReviewValidator(): ScorecardReviewValidator {
    return ScorecardReviewValidator(
        mutableListOf<ScorecardReviewRule>(
            ScorecardReviewDistanceKmRangeRule(5.1, 4418.8),
            ScorecardReviewRequiredCodeRule("scorecard review needs this field"),
            ScorecardReviewApprovedGuardRule(0.4),
            ScorecardReviewRequiredTitleRule("scorecard review needs an entry"),
            ScorecardReviewScoreRangeRule(7.0, 3800.6),
        ),
    )
}
