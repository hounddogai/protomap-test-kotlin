package com.acme.transport.linehaul.tender.digest

/** A rule a tender review must pass. Check returns its problems, or none. */
interface TenderReviewRule {
    fun check(item: TenderReview): MutableList<String>
}

data class TenderReviewRequiredSummaryRule(var message: String) : TenderReviewRule {
    override fun check(item: TenderReview): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderReviewPriorityRangeRule(var min: Int, var max: Int) : TenderReviewRule {
    override fun check(item: TenderReview): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class TenderReviewRequiredTitleRule(var message: String) : TenderReviewRule {
    override fun check(item: TenderReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderReviewCodeLengthRule(var maxLength: Int) : TenderReviewRule {
    override fun check(item: TenderReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderReviewCodePrefixRule(var prefix: String) : TenderReviewRule {
    override fun check(item: TenderReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender review. */
data class TenderReviewValidator(var rules: MutableList<TenderReviewRule>) {
    fun validate(item: TenderReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderReview>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderReviewValidator(): TenderReviewValidator {
    return TenderReviewValidator(
        mutableListOf<TenderReviewRule>(
            TenderReviewRequiredSummaryRule("tender review needs an entry"),
            TenderReviewPriorityRangeRule(16, 80),
            TenderReviewRequiredTitleRule("tender review needs this field"),
            TenderReviewCodeLengthRule(32),
            TenderReviewCodePrefixRule("X"),
        ),
    )
}
