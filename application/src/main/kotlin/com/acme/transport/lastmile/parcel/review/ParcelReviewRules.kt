package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.text.normalize

/** A rule a parcel review must pass. Check returns its problems, or none. */
interface ParcelReviewRule {
    fun check(item: ParcelReview): MutableList<String>
}

data class ParcelReviewAllowedCodeRule(var allowed: MutableList<String>) : ParcelReviewRule {
    override fun check(item: ParcelReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ParcelReviewCreatedAtRangeRule(var min: Int, var max: Int) : ParcelReviewRule {
    override fun check(item: ParcelReview): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelReviewTitleLengthRule(var maxLength: Int) : ParcelReviewRule {
    override fun check(item: ParcelReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelReviewContactEmailLengthRule(var maxLength: Int) : ParcelReviewRule {
    override fun check(item: ParcelReview): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelReviewRequiredContactEmailRule(var message: String) : ParcelReviewRule {
    override fun check(item: ParcelReview): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelReviewTrailerIdLengthRule(var maxLength: Int) : ParcelReviewRule {
    override fun check(item: ParcelReview): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel review. */
data class ParcelReviewValidator(var rules: MutableList<ParcelReviewRule>) {
    fun validate(item: ParcelReview): MutableList<String> {
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

    fun requireValid(item: ParcelReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelReview>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelReviewValidator(): ParcelReviewValidator {
    return ParcelReviewValidator(
        mutableListOf<ParcelReviewRule>(
            ParcelReviewAllowedCodeRule(mutableListOf<String>("batched", "weighted", "default")),
            ParcelReviewCreatedAtRangeRule(3600, 3600),
            ParcelReviewTitleLengthRule(64),
            ParcelReviewContactEmailLengthRule(86400),
            ParcelReviewRequiredContactEmailRule("parcel review needs this field"),
            ParcelReviewTrailerIdLengthRule(255),
        ),
    )
}
