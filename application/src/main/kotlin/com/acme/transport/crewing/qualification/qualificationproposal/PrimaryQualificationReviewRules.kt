package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.text.normalize

/** A rule a primary qualification review must pass. Check returns its problems, or none. */
interface PrimaryQualificationReviewRule {
    fun check(item: PrimaryQualificationReview): MutableList<String>
}

data class PrimaryQualificationReviewLabelLengthRule(var maxLength: Int) : PrimaryQualificationReviewRule {
    override fun check(item: PrimaryQualificationReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationReviewAllowedNotesRule(
    var allowed: MutableList<String>,
) : PrimaryQualificationReviewRule {
    override fun check(item: PrimaryQualificationReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationReviewRequiredCodeRule(var message: String) : PrimaryQualificationReviewRule {
    override fun check(item: PrimaryQualificationReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationReviewCodePrefixRule(var prefix: String) : PrimaryQualificationReviewRule {
    override fun check(item: PrimaryQualificationReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationReviewNotesLengthRule(var maxLength: Int) : PrimaryQualificationReviewRule {
    override fun check(item: PrimaryQualificationReview): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary qualification review. */
data class PrimaryQualificationReviewValidator(var rules: MutableList<PrimaryQualificationReviewRule>) {
    fun validate(item: PrimaryQualificationReview): MutableList<String> {
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

    fun requireValid(item: PrimaryQualificationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryQualificationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryQualificationReviewValidator(): PrimaryQualificationReviewValidator {
    return PrimaryQualificationReviewValidator(
        mutableListOf<PrimaryQualificationReviewRule>(
            PrimaryQualificationReviewLabelLengthRule(16),
            PrimaryQualificationReviewAllowedNotesRule(mutableListOf<String>("regional")),
            PrimaryQualificationReviewRequiredCodeRule("primary qualification review needs this field"),
            PrimaryQualificationReviewCodePrefixRule("X"),
            PrimaryQualificationReviewNotesLengthRule(86400),
        ),
    )
}
