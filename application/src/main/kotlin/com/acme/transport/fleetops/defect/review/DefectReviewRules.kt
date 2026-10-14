package com.acme.transport.fleetops.defect.review

/** A rule a defect review must pass. Check returns its problems, or none. */
interface DefectReviewRule {
    fun check(item: DefectReview): MutableList<String>
}

data class DefectReviewCodePrefixRule(var prefix: String) : DefectReviewRule {
    override fun check(item: DefectReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectReviewExpiresAtRangeRule(var min: Int, var max: Int) : DefectReviewRule {
    override fun check(item: DefectReview): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class DefectReviewNameLengthRule(var maxLength: Int) : DefectReviewRule {
    override fun check(item: DefectReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect review. */
data class DefectReviewValidator(var rules: MutableList<DefectReviewRule>) {
    fun validate(item: DefectReview): MutableList<String> {
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

    fun requireValid(item: DefectReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectReview>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectReviewValidator(): DefectReviewValidator {
    return DefectReviewValidator(
        mutableListOf<DefectReviewRule>(
            DefectReviewCodePrefixRule("OPS"),
            DefectReviewExpiresAtRangeRule(16, 120),
            DefectReviewNameLengthRule(255),
        ),
    )
}
