package com.acme.transport.crewing.dutyperiod.review

/** A rule a duty period review must pass. Check returns its problems, or none. */
interface DutyPeriodReviewRule {
    fun check(item: DutyPeriodReview): MutableList<String>
}

data class DutyPeriodReviewRequiredCurrencyRule(var message: String) : DutyPeriodReviewRule {
    override fun check(item: DutyPeriodReview): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodReviewCurrencyLengthRule(var maxLength: Int) : DutyPeriodReviewRule {
    override fun check(item: DutyPeriodReview): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodReviewCodeLengthRule(var maxLength: Int) : DutyPeriodReviewRule {
    override fun check(item: DutyPeriodReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period review. */
data class DutyPeriodReviewValidator(var rules: MutableList<DutyPeriodReviewRule>) {
    fun validate(item: DutyPeriodReview): MutableList<String> {
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

    fun requireValid(item: DutyPeriodReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodReview>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodReviewValidator(): DutyPeriodReviewValidator {
    return DutyPeriodReviewValidator(
        mutableListOf<DutyPeriodReviewRule>(
            DutyPeriodReviewRequiredCurrencyRule("duty period review needs an entry"),
            DutyPeriodReviewCurrencyLengthRule(16),
            DutyPeriodReviewCodeLengthRule(255),
        ),
    )
}
