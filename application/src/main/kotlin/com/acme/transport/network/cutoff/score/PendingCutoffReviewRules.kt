package com.acme.transport.network.cutoff.score

/** A rule a pending cutoff review must pass. Check returns its problems, or none. */
interface PendingCutoffReviewRule {
    fun check(item: PendingCutoffReview): MutableList<String>
}

data class PendingCutoffReviewRequiredChannelRule(var message: String) : PendingCutoffReviewRule {
    override fun check(item: PendingCutoffReview): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffReviewAttemptsRangeRule(var min: Int, var max: Int) : PendingCutoffReviewRule {
    override fun check(item: PendingCutoffReview): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffReviewRequiredLabelRule(var message: String) : PendingCutoffReviewRule {
    override fun check(item: PendingCutoffReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffReviewLabelLengthRule(var maxLength: Int) : PendingCutoffReviewRule {
    override fun check(item: PendingCutoffReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending cutoff review. */
data class PendingCutoffReviewValidator(var rules: MutableList<PendingCutoffReviewRule>) {
    fun validate(item: PendingCutoffReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCutoffReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCutoffReview>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCutoffReviewValidator(): PendingCutoffReviewValidator {
    return PendingCutoffReviewValidator(
        mutableListOf<PendingCutoffReviewRule>(
            PendingCutoffReviewRequiredChannelRule("pending cutoff review needs this field"),
            PendingCutoffReviewAttemptsRangeRule(120, 86400),
            PendingCutoffReviewRequiredLabelRule("pending cutoff review needs this field"),
            PendingCutoffReviewLabelLengthRule(3600),
        ),
    )
}
