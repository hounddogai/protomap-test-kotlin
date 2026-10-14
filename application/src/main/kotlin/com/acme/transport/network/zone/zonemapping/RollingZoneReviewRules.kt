package com.acme.transport.network.zone.zonemapping

/** A rule a rolling zone review must pass. Check returns its problems, or none. */
interface RollingZoneReviewRule {
    fun check(item: RollingZoneReview): MutableList<String>
}

data class RollingZoneReviewLabelLengthRule(var maxLength: Int) : RollingZoneReviewRule {
    override fun check(item: RollingZoneReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneReviewRequiredSummaryRule(var message: String) : RollingZoneReviewRule {
    override fun check(item: RollingZoneReview): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneReviewTitleLengthRule(var maxLength: Int) : RollingZoneReviewRule {
    override fun check(item: RollingZoneReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling zone review. */
data class RollingZoneReviewValidator(var rules: MutableList<RollingZoneReviewRule>) {
    fun validate(item: RollingZoneReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingZoneReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingZoneReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingZoneReviewValidator(): RollingZoneReviewValidator {
    return RollingZoneReviewValidator(
        mutableListOf<RollingZoneReviewRule>(
            RollingZoneReviewLabelLengthRule(86400),
            RollingZoneReviewRequiredSummaryRule("rolling zone review needs an entry"),
            RollingZoneReviewTitleLengthRule(32),
        ),
    )
}
