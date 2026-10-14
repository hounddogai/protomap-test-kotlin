package com.acme.transport.crewing.qualification.qualificationcycle

/** A rule a legacy qualification review must pass. Check returns its problems, or none. */
interface LegacyQualificationReviewRule {
    fun check(item: LegacyQualificationReview): MutableList<String>
}

data class LegacyQualificationReviewPhoneLengthRule(var maxLength: Int) : LegacyQualificationReviewRule {
    override fun check(item: LegacyQualificationReview): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyQualificationReviewRequiredPhoneRule(var message: String) : LegacyQualificationReviewRule {
    override fun check(item: LegacyQualificationReview): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyQualificationReviewUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : LegacyQualificationReviewRule {
    override fun check(item: LegacyQualificationReview): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyQualificationReviewRequiredTitleRule(var message: String) : LegacyQualificationReviewRule {
    override fun check(item: LegacyQualificationReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyQualificationReviewRequiredCodeRule(var message: String) : LegacyQualificationReviewRule {
    override fun check(item: LegacyQualificationReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyQualificationReviewCodeLengthRule(var maxLength: Int) : LegacyQualificationReviewRule {
    override fun check(item: LegacyQualificationReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy qualification review. */
data class LegacyQualificationReviewValidator(var rules: MutableList<LegacyQualificationReviewRule>) {
    fun validate(item: LegacyQualificationReview): MutableList<String> {
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

    fun requireValid(item: LegacyQualificationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyQualificationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyQualificationReviewValidator(): LegacyQualificationReviewValidator {
    return LegacyQualificationReviewValidator(
        mutableListOf<LegacyQualificationReviewRule>(
            LegacyQualificationReviewPhoneLengthRule(80),
            LegacyQualificationReviewRequiredPhoneRule("legacy qualification review needs an entry"),
            LegacyQualificationReviewUpdatedAtRangeRule(16, 255),
            LegacyQualificationReviewRequiredTitleRule("legacy qualification review needs an entry"),
            LegacyQualificationReviewRequiredCodeRule("legacy qualification review needs an entry"),
            LegacyQualificationReviewCodeLengthRule(1000),
        ),
    )
}
