package com.acme.transport.crewing.availability.adjustment

/** A rule a pending availability review must pass. Check returns its problems, or none. */
interface PendingAvailabilityReviewRule {
    fun check(item: PendingAvailabilityReview): MutableList<String>
}

data class PendingAvailabilityReviewCodeLengthRule(var maxLength: Int) : PendingAvailabilityReviewRule {
    override fun check(item: PendingAvailabilityReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityReviewNotesLengthRule(var maxLength: Int) : PendingAvailabilityReviewRule {
    override fun check(item: PendingAvailabilityReview): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityReviewCapacityRangeRule(
    var min: Int,
    var max: Int,
) : PendingAvailabilityReviewRule {
    override fun check(item: PendingAvailabilityReview): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class PendingAvailabilityReviewLabelLengthRule(var maxLength: Int) : PendingAvailabilityReviewRule {
    override fun check(item: PendingAvailabilityReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityReviewRequiredTitleRule(var message: String) : PendingAvailabilityReviewRule {
    override fun check(item: PendingAvailabilityReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityReviewTitleLengthRule(var maxLength: Int) : PendingAvailabilityReviewRule {
    override fun check(item: PendingAvailabilityReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending availability review. */
data class PendingAvailabilityReviewValidator(var rules: MutableList<PendingAvailabilityReviewRule>) {
    fun validate(item: PendingAvailabilityReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingAvailabilityReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAvailabilityReview>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAvailabilityReviewValidator(): PendingAvailabilityReviewValidator {
    return PendingAvailabilityReviewValidator(
        mutableListOf<PendingAvailabilityReviewRule>(
            PendingAvailabilityReviewCodeLengthRule(16),
            PendingAvailabilityReviewNotesLengthRule(16),
            PendingAvailabilityReviewCapacityRangeRule(80, 3600),
            PendingAvailabilityReviewLabelLengthRule(255),
            PendingAvailabilityReviewRequiredTitleRule("pending availability review needs a value"),
            PendingAvailabilityReviewTitleLengthRule(1000),
        ),
    )
}
