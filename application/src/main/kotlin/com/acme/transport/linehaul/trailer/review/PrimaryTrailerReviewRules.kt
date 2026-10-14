package com.acme.transport.linehaul.trailer.review

/** A rule a primary trailer review must pass. Check returns its problems, or none. */
interface PrimaryTrailerReviewRule {
    fun check(item: PrimaryTrailerReview): MutableList<String>
}

data class PrimaryTrailerReviewRequiredPhoneRule(var message: String) : PrimaryTrailerReviewRule {
    override fun check(item: PrimaryTrailerReview): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerReviewRequiredCodeRule(var message: String) : PrimaryTrailerReviewRule {
    override fun check(item: PrimaryTrailerReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerReviewLabelLengthRule(var maxLength: Int) : PrimaryTrailerReviewRule {
    override fun check(item: PrimaryTrailerReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerReviewMinimumRangeRule(var min: Double, var max: Double) : PrimaryTrailerReviewRule {
    override fun check(item: PrimaryTrailerReview): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerReviewRequiredLabelRule(var message: String) : PrimaryTrailerReviewRule {
    override fun check(item: PrimaryTrailerReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary trailer review. */
data class PrimaryTrailerReviewValidator(var rules: MutableList<PrimaryTrailerReviewRule>) {
    fun validate(item: PrimaryTrailerReview): MutableList<String> {
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

    fun requireValid(item: PrimaryTrailerReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTrailerReview>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTrailerReviewValidator(): PrimaryTrailerReviewValidator {
    return PrimaryTrailerReviewValidator(
        mutableListOf<PrimaryTrailerReviewRule>(
            PrimaryTrailerReviewRequiredPhoneRule("primary trailer review needs an entry"),
            PrimaryTrailerReviewRequiredCodeRule("primary trailer review needs this field"),
            PrimaryTrailerReviewLabelLengthRule(3600),
            PrimaryTrailerReviewMinimumRangeRule(7.3, 2806.7),
            PrimaryTrailerReviewRequiredLabelRule("primary trailer review needs this field"),
        ),
    )
}
