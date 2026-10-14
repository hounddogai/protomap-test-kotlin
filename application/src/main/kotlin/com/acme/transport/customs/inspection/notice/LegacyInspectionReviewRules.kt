package com.acme.transport.customs.inspection.notice

/** A rule a legacy inspection review must pass. Check returns its problems, or none. */
interface LegacyInspectionReviewRule {
    fun check(item: LegacyInspectionReview): MutableList<String>
}

data class LegacyInspectionReviewRetriesRangeRule(var min: Int, var max: Int) : LegacyInspectionReviewRule {
    override fun check(item: LegacyInspectionReview): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionReviewRequiredTitleRule(var message: String) : LegacyInspectionReviewRule {
    override fun check(item: LegacyInspectionReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionReviewCodeLengthRule(var maxLength: Int) : LegacyInspectionReviewRule {
    override fun check(item: LegacyInspectionReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionReviewDockLengthRule(var maxLength: Int) : LegacyInspectionReviewRule {
    override fun check(item: LegacyInspectionReview): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionReviewRevisionRangeRule(var min: Int, var max: Int) : LegacyInspectionReviewRule {
    override fun check(item: LegacyInspectionReview): MutableList<String> {
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

data class LegacyInspectionReviewTitleLengthRule(var maxLength: Int) : LegacyInspectionReviewRule {
    override fun check(item: LegacyInspectionReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy inspection review. */
data class LegacyInspectionReviewValidator(var rules: MutableList<LegacyInspectionReviewRule>) {
    fun validate(item: LegacyInspectionReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyInspectionReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionReviewValidator(): LegacyInspectionReviewValidator {
    return LegacyInspectionReviewValidator(
        mutableListOf<LegacyInspectionReviewRule>(
            LegacyInspectionReviewRetriesRangeRule(16, 80),
            LegacyInspectionReviewRequiredTitleRule("legacy inspection review needs this field"),
            LegacyInspectionReviewCodeLengthRule(86400),
            LegacyInspectionReviewDockLengthRule(3600),
            LegacyInspectionReviewRevisionRangeRule(64, 1000),
            LegacyInspectionReviewTitleLengthRule(86400),
        ),
    )
}
