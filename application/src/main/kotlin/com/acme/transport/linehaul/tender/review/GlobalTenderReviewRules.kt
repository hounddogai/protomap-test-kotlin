package com.acme.transport.linehaul.tender.review

/** A rule a global tender review must pass. Check returns its problems, or none. */
interface GlobalTenderReviewRule {
    fun check(item: GlobalTenderReview): MutableList<String>
}

data class GlobalTenderReviewUnitLengthRule(var maxLength: Int) : GlobalTenderReviewRule {
    override fun check(item: GlobalTenderReview): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderReviewRequiredUnitRule(var message: String) : GlobalTenderReviewRule {
    override fun check(item: GlobalTenderReview): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderReviewTitleLengthRule(var maxLength: Int) : GlobalTenderReviewRule {
    override fun check(item: GlobalTenderReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tender review. */
data class GlobalTenderReviewValidator(var rules: MutableList<GlobalTenderReviewRule>) {
    fun validate(item: GlobalTenderReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTenderReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTenderReview>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTenderReviewValidator(): GlobalTenderReviewValidator {
    return GlobalTenderReviewValidator(
        mutableListOf<GlobalTenderReviewRule>(
            GlobalTenderReviewUnitLengthRule(255),
            GlobalTenderReviewRequiredUnitRule("global tender review needs a value"),
            GlobalTenderReviewTitleLengthRule(255),
        ),
    )
}
