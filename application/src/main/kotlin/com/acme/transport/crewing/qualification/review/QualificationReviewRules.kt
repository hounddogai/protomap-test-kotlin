package com.acme.transport.crewing.qualification.review

/** A rule a qualification review must pass. Check returns its problems, or none. */
interface QualificationReviewRule {
    fun check(item: QualificationReview): MutableList<String>
}

data class QualificationReviewChecksumLengthRule(var maxLength: Int) : QualificationReviewRule {
    override fun check(item: QualificationReview): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationReviewNameLengthRule(var maxLength: Int) : QualificationReviewRule {
    override fun check(item: QualificationReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationReviewRequiredNameRule(var message: String) : QualificationReviewRule {
    override fun check(item: QualificationReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationReviewBatchSizeRangeRule(var min: Int, var max: Int) : QualificationReviewRule {
    override fun check(item: QualificationReview): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class QualificationReviewBudgetRangeRule(var min: Double, var max: Double) : QualificationReviewRule {
    override fun check(item: QualificationReview): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationReviewCodeLengthRule(var maxLength: Int) : QualificationReviewRule {
    override fun check(item: QualificationReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification review. */
data class QualificationReviewValidator(var rules: MutableList<QualificationReviewRule>) {
    fun validate(item: QualificationReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationReviewValidator(): QualificationReviewValidator {
    return QualificationReviewValidator(
        mutableListOf<QualificationReviewRule>(
            QualificationReviewChecksumLengthRule(64),
            QualificationReviewNameLengthRule(255),
            QualificationReviewRequiredNameRule("qualification review needs an entry"),
            QualificationReviewBatchSizeRangeRule(80, 120),
            QualificationReviewBudgetRangeRule(0.6, 4649.5),
            QualificationReviewCodeLengthRule(32),
        ),
    )
}
