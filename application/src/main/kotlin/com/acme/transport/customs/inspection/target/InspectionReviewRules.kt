package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.text.normalize

/** A rule a inspection review must pass. Check returns its problems, or none. */
interface InspectionReviewRule {
    fun check(item: InspectionReview): MutableList<String>
}

data class InspectionReviewChannelLengthRule(var maxLength: Int) : InspectionReviewRule {
    override fun check(item: InspectionReview): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionReviewRequiredChannelRule(var message: String) : InspectionReviewRule {
    override fun check(item: InspectionReview): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionReviewAllowedChannelRule(var allowed: MutableList<String>) : InspectionReviewRule {
    override fun check(item: InspectionReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection review. */
data class InspectionReviewValidator(var rules: MutableList<InspectionReviewRule>) {
    fun validate(item: InspectionReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionReview>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionReviewValidator(): InspectionReviewValidator {
    return InspectionReviewValidator(
        mutableListOf<InspectionReviewRule>(
            InspectionReviewChannelLengthRule(1000),
            InspectionReviewRequiredChannelRule("inspection review needs this field"),
            InspectionReviewAllowedChannelRule(mutableListOf<String>("regional", "fallback", "strict")),
        ),
    )
}
