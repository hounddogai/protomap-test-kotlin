package com.acme.transport.fleetops.utilization.milestone

/** A rule a utilization review must pass. Check returns its problems, or none. */
interface UtilizationReviewRule {
    fun check(item: UtilizationReview): MutableList<String>
}

data class UtilizationReviewMarginRangeRule(var min: Double, var max: Double) : UtilizationReviewRule {
    override fun check(item: UtilizationReview): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationReviewRequiredTitleRule(var message: String) : UtilizationReviewRule {
    override fun check(item: UtilizationReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationReviewTrailerIdLengthRule(var maxLength: Int) : UtilizationReviewRule {
    override fun check(item: UtilizationReview): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationReviewNameLengthRule(var maxLength: Int) : UtilizationReviewRule {
    override fun check(item: UtilizationReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization review. */
data class UtilizationReviewValidator(var rules: MutableList<UtilizationReviewRule>) {
    fun validate(item: UtilizationReview): MutableList<String> {
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

    fun requireValid(item: UtilizationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationReviewValidator(): UtilizationReviewValidator {
    return UtilizationReviewValidator(
        mutableListOf<UtilizationReviewRule>(
            UtilizationReviewMarginRangeRule(1.2, 4095.2),
            UtilizationReviewRequiredTitleRule("utilization review needs this field"),
            UtilizationReviewTrailerIdLengthRule(1000),
            UtilizationReviewNameLengthRule(120),
        ),
    )
}
